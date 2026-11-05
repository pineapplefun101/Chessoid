import chess
import sqlite3
import random

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


def get_test_puzzle(db_path):
    """Connects to the SQLite DB and fetches one puzzle to verify it works."""
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Grab the first puzzle we find
    cursor.execute("SELECT Rating, FEN, Moves FROM puzzles LIMIT 1")
    row = cursor.fetchone()
    conn.close()

    if row:
        return f"Success! Rating: {row[0]}, FEN: {row[1]}"
    return "Database is empty or missing."


# Store the solution for the current puzzle
current_solution = []

def load_random_puzzle(db_path):
    global current_solution
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()

    # Get the max row ID to randomly select a puzzle in O(1) time
    cursor.execute("SELECT MAX(rowid) FROM puzzles")
    max_id = cursor.fetchone()[0]

    rand_id = random.randint(1, max_id)
    cursor.execute("SELECT FEN, Moves FROM puzzles WHERE rowid = ?", (rand_id,))
    row = cursor.fetchone()
    conn.close()

    if row:
        fen = row[0]
        # Moves are stored as space-separated strings (e.g., "e2e4 e7e5")
        moves = row[1].split(' ')

        # Crucial: Sync the python-chess engine to this puzzle's starting position
        board.set_fen(fen)
        current_solution = moves

        return fen
    return ""