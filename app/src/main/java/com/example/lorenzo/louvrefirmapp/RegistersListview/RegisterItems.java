package com.example.lorenzo.louvrefirmapp.RegistersListview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


 // TODO sistemare javadoc
public class RegisterItems
{
    List<Item> ItemsList = new ArrayList<Item>();
    Map<String, Item> ItemsMap = new HashMap<String, Item>();


    public List<Item> getItemsList()
    {
        return ItemsList;
    }

    public Map<String, Item> getItemsMap()
    {
        return ItemsMap;
    }


    public void addItem(Item item) {
        ItemsList.add(item);
        ItemsMap.put(item.id, item);
    }


    public static class Item
    {
        String id;
        String content;


        public String getId()
        {
            return id;
        }

        public String getContent()
        {
            return content;
        }

        public void setContent(String content)
        {
            this.content = content;
        }


        public Item(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
