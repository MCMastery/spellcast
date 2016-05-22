package com.spellcastrpg.main.objects.gui;

import com.spellcastrpg.main.Anchor;
import com.spellcastrpg.main.IAnchor;
import com.spellcastrpg.main.Input;
import com.spellcastrpg.main.SpellCast;
import com.spellcastrpg.main.geometry.Rectangle;
import com.spellcastrpg.main.geometry.Vector2d;
import com.spellcastrpg.main.items.ItemObject;
import com.spellcastrpg.main.rendering.RGBAColor;
import com.spellcastrpg.main.rendering.Renderer;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends GUIContainer {
    private Map<Integer, ItemObject> contents;
    private int selection, size;
    private RGBAColor  selectionColor;
    private ItemInfoBox itemInfo;

    public Inventory() {
        this.contents = new HashMap<>();
        this.selection = 0;
        this.size = 10;
        this.selectionColor = new RGBAColor(1, 0, 1, 0.75);
        this.itemInfo = null;
        setAnchor(new Anchor(IAnchor.HAnchor.CENTER, IAnchor.VAnchor.CENTER));
        updateBounds();
    }

    public Map<Integer, ItemObject> getContents() {
        return this.contents;
    }
    public ItemObject getItem(int slot) {
        if (slot < 0 || slot >= this.size)
            return null;
        return this.contents.get(slot);
    }
    public boolean addItem(ItemObject item) {
        for (int slot = 0; slot < this.size; slot++) {
            if (this.contents.get(slot) == null) {
                this.contents.put(slot, item);
                return true;
            }
        }
        return false;
    }
    public ItemObject removeItem(int slot) {
        return this.contents.remove(slot);
    }
    public boolean setItem(int slot, ItemObject item) {
        if (slot < 0 || slot >= this.size)
            return false;
        removeItem(slot);
        this.contents.put(slot, item);
        return true;
    }

    public int getSize() {
        return this.size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public int getSelection() {
        return this.selection;
    }
    public void setSelection(int selection) {
        while (selection < 0)
            selection = this.size + selection;
        while (selection >= this.size)
            selection = Math.abs(this.size - selection);
        this.selection = selection;
    }
    public ItemObject getSelectedItem() {
        return getItem(this.selection);
    }


    public RGBAColor getSelectionColor() {
        return this.selectionColor;
    }
    public void setSelectionColor(RGBAColor selectionColor) {
        this.selectionColor = selectionColor;
    }


    public void updateBounds() {
        // remember our anchor is in the center
        setBounds(new Rectangle(SpellCast.INSTANCE.getWindowSize().getWidth() / 2, ItemObject.SIZE / 2, this.size * ItemObject.SIZE, ItemObject.SIZE));
    }


    @Override
    public void update() {
        updateBounds();
        ItemObject selectedItem = getSelectedItem();
        if (selectedItem != null && selectedItem.canUse() && Input.INSTANCE.mouseDown(Input.MOUSE_LEFT))
            selectedItem.use();

        // check if we should show the item info box
        Vector2d mousePos = Input.INSTANCE.getMouseScreenPosition();
        if (getBounds().contains(mousePos)) {
            mousePos = mousePos.subtract(getPosition());
            int slot = (int) Math.floor(mousePos.getX() / ItemObject.SIZE);
            showItemInfo(slot);
        } else {
            if (this.itemInfo != null) {
                this.itemInfo.destroy();
                this.itemInfo = null;
            }
        }
    }

    public void showItemInfo(int slot) {
        ItemObject item = getItem(slot);
        if (item == null) {
            if (this.itemInfo != null)
                this.itemInfo.destroy();
            this.itemInfo = null;
            return;
        }
        if (this.itemInfo != null) {
            this.itemInfo.destroy();
            this.itemInfo = null;
        }
        this.itemInfo = new ItemInfoBox(item);
        // add ItemObject.SIZE / 2 to make it centered
        Vector2d position = new Vector2d(slot * ItemObject.SIZE + ItemObject.SIZE / 2 + getPosition().getX(), getBounds().getY2());
        this.itemInfo.setPosition(position);
        this.itemInfo.init();
    }

    @Override
    public void mouseScroll(double amount) {
        setSelection(this.selection + (int) Math.round(amount));
    }

    @Override
    public void render(Renderer r) {
        super.render(r);
        for (int slot = 0; slot < this.size; slot++) {
            ItemObject item = getItem(slot);
            if (item == null || item.getImage() == null)
                continue;
            r.drawRoundedImage(item.getImage(), getRadius(), getRadius(), new Vector2d(getBounds().getX() + slot * ItemObject.SIZE, getBounds().getY()));
        }
        // add size / 2 so it is centered
        Vector2d selectionPos = getBounds().getPosition().add(this.selection * ItemObject.SIZE + ItemObject.SIZE / 2, ItemObject.SIZE);
        // draw selection arrow
        r.fillPolygon(this.selectionColor, selectionPos, selectionPos.add(-8, 8), selectionPos.add(8, 8));
        // draw selection circle
        Rectangle selectionBounds = getBounds().translate(this.selection * ItemObject.SIZE, 0).setSize(ItemObject.SIZE, ItemObject.SIZE);
        r.drawRoundedRect(selectionBounds, getRadius(), getRadius(), 1, this.selectionColor);
    }
}
