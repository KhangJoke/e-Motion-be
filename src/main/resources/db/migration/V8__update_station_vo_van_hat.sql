-- Update station 1 to Vo Van Hat branch for realistic testing
UPDATE stations
SET station_name = 'Trạm Sạc & Thuê Xe Võ Văn Hát',
    station_address = '264/4 Võ Văn Hát, Phường Long Trường, TP. Thủ Đức, TP. Hồ Chí Minh',
    station_latitude = 10.815476,
    station_longitude = 106.807322
WHERE station_id = 1;
