package ru.project.item.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.project.item.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-server", url = "http://user-server:8081")
public interface UserForItemClient {
    @GetMapping("/users/{userId}")
    UserDto getUserById(@PathVariable("userId") long userId);

    @GetMapping("/users/comments")
    List<UserDto> getUsersByUserIds(@RequestParam("userIds") List<Long> userIds);
}
