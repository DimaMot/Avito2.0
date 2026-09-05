package ru.project.booking.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.project.booking.dto.UserDto;

import java.util.List;

@FeignClient(name = "user-server", url = "http://user-server:8081")
public interface UserForBookingClient {
    @GetMapping("/users/{userId}")
    UserDto getUserById(@PathVariable("userId") long userId);

    @GetMapping("/users/comments")
    List<UserDto> getUsersByIds(@RequestParam("userIds") List<Long> usersIds);
}