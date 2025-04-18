package ru.practicum.mapper;

import ru.practicum.model.Location;
import ru.practicum.model.dto.LocationDto;

public class LocationMapper {
    public static Location fromLocationDto(LocationDto locationDto) {
        return Location.builder().lat(locationDto.getLat()).lon(locationDto.getLon()).build();
    }

    public static LocationDto toLocationDto(Location location) {
        return LocationDto.builder().lat(location.getLat()).lon(location.getLon()).build();
    }
}
