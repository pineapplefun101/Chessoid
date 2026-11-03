import chess

# Create a persistent board instance to track the game state
board = chess.Board()

def get_legal_moves(square_name):
    """
    Given a starting square (e.g., 'e2'), returns a list of legal destination squares.
    """
    square_index = chess.parse_square(square_name)
    legal_moves = []

    for move in board.legal_moves:
        if move.from_square == square_index:
            legal_moves.append(chess.square_name(move.to_square))

    return legal_moves

def get_fen():
    """Returns the current board state as a FEN string to feed to Stockfish."""
    return board.fen()

def make_move(move_uci):
    move = chess.Move.from_uci(move_uci)
    if move in board.legal_moves:
        board.push(move)
        return True
    return False
