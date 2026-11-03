module ChessApp {

    enum PieceColor {
        COLORWHITE,
        COLORBLACK,
        COLORNONE
    };

    enum PieceType {
        PIECEPAWN,
        PIECEKNIGHT,
        PIECEBISHOP,
        PIECEROOK,
        PIECEQUEEN,
        PIECEKING,
        PIECEEMPTY
    };

    enum PlayerRole {
        ROLEWHITE,
        ROLEBLACK,
        ROLESPECTATOR
    };

    enum GameStatus {
        WAITINGFORPLAYERS,
        INPROGRESS,
        CHECKWHITE,
        CHECKBLACK,
        CHECKMATEWHITEWINS,
        CHECKMATEBLACKWINS,
        STALEMATE,
        RESIGNEDWHITEWINS,
        RESIGNEDBLACKWINS,
        ABORTED
    };

    enum TierLevel {
        TIERBRONZE,
        TIERSILVER,
        TIERGOLD,
        TIERPLATINUM,
        TIERDIAMOND
    };

    struct Position {
        int row;
        int col;
    };

    struct ChessPiece {
        PieceType type;
        PieceColor color;
    };

    sequence<ChessPiece> BoardRow;
    sequence<BoardRow> BoardMatrix;

    struct MoveRecord {
        string moveNotation;
        string playerNickname;
        PieceColor playerColor;
        Position fromPos;
        Position toPos;
        long timestamp;
    };

    sequence<MoveRecord> MoveHistory;
    sequence<ChessPiece> PieceList;

    struct PlayerProfile {
        string nickname;
        int points;
        int wins;
        int losses;
        int draws;
        TierLevel tier;
    };

    sequence<PlayerProfile> PlayerLeaderboard;

    struct RoomSummary {
        string roomCode;
        string whitePlayer;
        string blackPlayer;
        int spectatorCount;
        GameStatus status;
    };

    sequence<RoomSummary> RoomList;

    struct GameState {
        string roomCode;
        BoardMatrix board;
        PieceColor currentTurn;
        GameStatus status;
        string whitePlayerNickname;
        string blackPlayerNickname;
        int whitePoints;
        int blackPoints;
        PieceList whiteCapturedPieces;
        PieceList blackCapturedPieces;
        MoveHistory moves;
        int spectatorCount;
        long version;
    };

    struct JoinRoomResult {
        PlayerRole role;
        PieceColor assignedColor;
        GameState state;
    };

    exception ChessException {
        string reason;
    };

    exception InvalidMoveException extends ChessException {};
    exception RoomNotFoundException extends ChessException {};
    exception NotPlayerTurnException extends ChessException {};
    exception UnauthorizedActionException extends ChessException {};

    interface ChessObserver {
        void onGameStateChanged(GameState newState);
        void onGameFinished(GameState finalState, string winnerNickname, int pointsDelta);
    };

    interface ChessService {
        PlayerProfile getProfile(string nickname);
        PlayerLeaderboard getLeaderboard();

        string createRoom(string creatorNickname);
        JoinRoomResult joinRoom(string roomCode, string nickname) throws RoomNotFoundException, ChessException;
        void registerObserver(string roomCode, string nickname, ChessObserver* observer);
        void unregisterObserver(string roomCode, string nickname);

        GameState getGameState(string roomCode) throws RoomNotFoundException;
        bool makeMove(string roomCode, string nickname, Position fromPos, Position toPos) throws InvalidMoveException, NotPlayerTurnException, RoomNotFoundException;
        void resign(string roomCode, string nickname) throws RoomNotFoundException, UnauthorizedActionException;
        void leaveRoom(string roomCode, string nickname) throws RoomNotFoundException;

        RoomList getActiveRooms();
    };

};
