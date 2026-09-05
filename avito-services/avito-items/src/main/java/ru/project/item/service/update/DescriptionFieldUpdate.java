package ru.project.item.service.update;

import org.springframework.stereotype.Component;
import ru.project.item.dto.ItemDto;
import ru.project.item.model.Item;

@Component
public class DescriptionFieldUpdate implements ItemFieldUpdate {
    @Override
    public boolean support(ItemDto itemField) {
        return itemField.description() != null;
    }

    @Override
    public void updateField(Item old, ItemDto newField) {
        old.setDescription(newField.description());
    }
}
