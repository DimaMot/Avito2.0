package ru.project.item.service.update;

import ru.project.item.dto.ItemDto;
import ru.project.item.model.Item;

public interface ItemFieldUpdate {
    boolean support(ItemDto itemField);

    void updateField(Item old, ItemDto newField);
}
