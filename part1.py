
import numpy as np
import pandas as pd


dataset = pd.read_csv('data.csv')

print(dataset)

userInput = input("Enter a value to display column: ID(0), Name(1), Description(2): ")

print(userInput)

if(userInput == '0'):
    print(dataset['ID'])
elif(userInput == '1'):
    print(dataset['Name'])
elif(userInput == '2'):
    print(dataset['Description'])
else:
    print('Error! Invalid Entry')

    


