
import pandas as pd
import part1 as p1

class UI:
    
    table = p1.backend()
    
    def printTable(this):
        this.table.printData()
    
    def editName(this, id):
        # Find the row corresponding to the given ID
        try:
            row = this.table.dataset.loc[id]  # Assuming 'id' is the index
        except KeyError:
            print(f"ID {id} not found in the dataset.")
            return

        # Prompt the user for the new name
        new_name = input("Enter the new name: ")
        
        # Update the name field directly in the DataFrame
        this.table.dataset.at[id, 'Name'] = new_name
        
    
    def saveFile(this):
        this.table.dataset.to_csv('data.csv', index=False)
        

        
        
        
    
    def chooseInput(this):
        print("Choose Input[0-3]!")
        
        userInput = int(input())
        
        if(userInput == 0):
            print("!!!")
            this.printTable()
            
        if(userInput == 1):
            row = int(input())
            
            this.editName(row)
            
        if(userInput == 2):
            this.saveFile()
        
            


newStuff = UI()



while True:
    newStuff.chooseInput()
