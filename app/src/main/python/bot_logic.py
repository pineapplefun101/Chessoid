import chess
import chess.engine

# Isolated board specifically for full matches
board = chess.Board()
engine = None

def init_engine(binary_path, elo):
    global engine
    # Close any existing engine ghost processes
    if engine is not None:
        engine.quit()

    # Boot up the Stockfish binary
    engine = chess.engine.SimpleEngine.popen_uci(binary_path)

    # Force Stockfish to respect the human-level Elo rating
    engine.configure({"UCI_LimitStrength": True, "UCI_Elo": elo})

def get_legal_moves(square_name):
    square_index = chess.parse_square(square_name)
    legal_destinations = []
    for move in board.legal_moves:
        if move.from_square == square_index:
            legal_destinations.append(chess.square_name(move.to_square))
    return legal_destinations

def make_move(move_uci):
    move = chess.Move.from_uci(move_uci)
    if move in board.legal_moves:
        board.push(move)
        return True
    return False

def get_bot_reply():
    if not engine or board.is_game_over():
        return "GAME_OVER"

    # Give the engine 0.5 seconds to think.
    # At lower Elos, it instantly finds bad moves. At high Elos, it needs time.
    result = engine.play(board, chess.engine.Limit(time=0.5))

    # Push the bot's chosen move to our internal board
    board.push(result.move)
    return result.move.uci()

def reset_board():
    global board
    board = chess.Board()

def close_engine():
    global engine
    if engine:
        engine.quit()
        engine = None