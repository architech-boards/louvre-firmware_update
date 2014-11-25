package com.example.lorenzo.louvrefirmapp.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 * TODO sistemare javadoc
 */
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
