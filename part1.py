
import numpy as np
import pandas as pd

class backend:
        
    dataset = pd.read_csv('data.csv')

    def printData(this):
        print(this.dataset)

    def printDataCol(this, col):
        print(this.dataset.iloc[:,col])
        
    
    


back = backend()

back.printDataCol(1)