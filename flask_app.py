from flask import Flask
from flask_cors import CORS
from models import db, ProcessedFile
from routes import image_bp

app = Flask(__name__)
CORS(app, origins="http://localhost:5173")

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+mysqlconnector://root:rabia@localhost/data_augmenter'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db.init_app(app)

app.register_blueprint(image_bp)

with app.app_context():
    db.create_all()

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5000)
