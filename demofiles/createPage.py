from flask import Flask, render_template
import pandas as pd

app = Flask(__name__)

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/review')
def review():
    return render_template('reviewPage.html')



@app.route('/database')
def index():
    df = pd.read_csv('geonames.csv')  # Load CSV
    df = df.head(19)
    table = df.to_html(classes='table table-striped', index=False)  # Convert to HTML table
    return render_template('database.html', table=table)

if __name__ == '__main__':
    app.run(debug=True, port=5000)
