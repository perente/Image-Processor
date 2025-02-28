from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()

class ProcessedFile(db.Model):
    __tablename__ = 'processed_files'
    id = db.Column(db.Integer, primary_key=True)
    filename = db.Column(db.String(255), nullable=False)
    processed_at = db.Column(db.DateTime, default=db.func.now())
    actions = db.Column(db.Text, nullable=False)
