package ru.project.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "item-server", url = "http://item-server:8082")
public interface ItemClientForUsers {
    @DeleteMapping("/items/owner/{userId}/delete-all")
    void deleteAllById(@PathVariable("userId") long userId);
}
