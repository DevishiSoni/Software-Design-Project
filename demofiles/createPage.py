from flask import Flask, render_template_string
import pandas as pd

app = Flask(__name__)

@app.route('/')
def index():
    df = pd.read_csv('geonames.csv')  # Load CSV
    table = df.to_html(classes='table table-striped', index=False)  # Convert to HTML table
    return render_template_string("""
        <html>
        <head>
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        </head>
        <body>
            <div class="container">
                <h2>CSV Data</h2>
                {{ table | safe }}
            </div>
        </body>
        </html>
    """, table=table)

if __name__ == '__main__':
    app.run(debug=True)
