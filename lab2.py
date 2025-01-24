import pandas as pd
import numpy as np
import json

# Milestone 1: Create and Read Sample Catalog Data

# Step 1: Create a flat file with sample catalog data
data = {
    "ID": [1, 2, 3],
    "Name": ["Item1", "Item2", "Item3"],
    "Description": ["Description of Item1", "Description of Item2", "Description of Item3"]
}
df = pd.DataFrame(data)
df.to_csv("catalog.csv", index=False)

# Step 2: Read data from the CSV file and parse it into a usable format
def load_catalog(file_path):
    df = pd.read_csv(file_path)
    return df.to_dict(orient="records")
1
catalog = load_catalog("catalog.csv")

# Milestone 2: Front-End Interaction

def display_menu():
    print("\nCatalog Menu:")
    print("1. View Catalog")
    print("2. View Item Details")
    print("3. Add New Item")
    print("4. Edit Existing Item")
    print("5. Exit")

# Function to display the catalog items
def view_catalog():
    print("\nCatalog Items:")
    for item in catalog:
        print(f"ID: {item['ID']}, Name: {item['Name']}")

# Function to display item details
def view_item_details():
    try:
        item_id = int(input("Enter the item ID to view details: "))
        item = next((i for i in catalog if i["ID"] == item_id), None)
        if item:
            print(json.dumps(item, indent=4))
        else:
            print("Item not found.")
    except ValueError:
        print("Invalid input. Please enter a numeric ID.")

# Milestone 3: Adding Core Functionality

# Function to add a new item
def add_item():
    try:
        new_id = int(input("Enter new item ID: "))
        new_name = input("Enter new item name: ").strip()
        new_description = input("Enter new item description: ").strip()

        if not new_name or not new_description:
            print("Name and description cannot be empty.")
            return

        if any(i["ID"] == new_id for i in catalog):
            print("Item with this ID already exists.")
            return

        catalog.append({"ID": new_id, "Name": new_name, "Description": new_description})
        print("Item added successfully.")
    except ValueError:
        print("Invalid input. Please enter valid values.")

# Function to edit an existing item
def edit_item():
    try:
        item_id = int(input("Enter the item ID to edit: "))
        item = next((i for i in catalog if i["ID"] == item_id), None)

        if not item:
            print("Item not found.")
            return

        new_name = input(f"Enter new name (current: {item['Name']}): ").strip()
        new_description = input(f"Enter new description (current: {item['Description']}): ").strip()

        if new_name:
            item["Name"] = new_name
        if new_description:
            item["Description"] = new_description

        print("Item updated successfully.")
    except ValueError:
        print("Invalid input. Please enter a numeric ID.")

# Main function to handle menu interactions
def main():
    while True:
        display_menu()
        choice = input("Select an option: ").strip()

        if choice == "1":
            view_catalog()
        elif choice == "2":
            view_item_details()
        elif choice == "3":
            add_item()
        elif choice == "4":
            edit_item()
        elif choice == "5":
            print("Exiting the program.")
            break
        else:
            print("Invalid choice. Please select a valid option.")

if __name__ == "__main__":
    main()
