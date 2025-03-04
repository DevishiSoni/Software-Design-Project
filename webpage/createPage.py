from flask import Flask, render_template
import pandas as pd

app = Flask(__name__)

df = pd.read_csv('geonames.csv')

@app.route('/')
def home():
    return render_template('homepage.html')

@app.route('/review')
def review(location_id):
    return render_template('reviewPage.html')



@app.route('/catalogue')
def index():
    display = df.sort_values(by=["Relevance at Scale"], ascending=False)
    
    display = display.head(75)
    
    #df["Actions"] = df.index.map(lambda i: f'<button onclick="handleClick({i})">Click</button>')
    
    
    #df = df.drop(columns=['Source', 'Toponymic Feature ID', 'ISO Language Code'
     #                     , 'Language', 'Syllabic Form', 'CGNDB ID', 'Generic Term',
      #                    'Generic Category', 
       #                   ], axis=1)
    
    display = display.loc[:, ['Geographical Name', 'Generic Term']]
    
    
    #print(display.head(10))
    
    table = display.to_html(classes='table table-striped', index=False)  # Convert to HTML table
    
    
    return render_template('database.html', table=table)

if __name__ == '__main__':
    app.run(debug=True, port=5000)
