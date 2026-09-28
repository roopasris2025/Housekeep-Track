package com.stayease.housekeeping.config;

import com.stayease.housekeeping.entity.Housekeeper;
import com.stayease.housekeeping.entity.Room;
import com.stayease.housekeeping.repository.HousekeeperRepository;
import com.stayease.housekeeping.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Adds a few sample rooms and housekeepers the first time the app starts (only if the tables are empty). */
@Component
public class DataSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final HousekeeperRepository housekeeperRepository;

    public DataSeeder(RoomRepository roomRepository, HousekeeperRepository housekeeperRepository) {
        this.roomRepository = roomRepository;
        this.housekeeperRepository = housekeeperRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            String[] types = {"Single", "Double", "Deluxe", "Suite"};
            for (int floor = 1; floor <= 3; floor++) {
                for (int n = 1; n <= 4; n++) {
                    Room r = new Room();
                    r.setRoomNumber(String.valueOf(floor * 100 + n));
                    r.setRoomType(types[n - 1]);
                    r.setFloor(floor);
                    roomRepository.save(r);      // status defaults to READY
                }
            }
        }
        if (housekeeperRepository.count() == 0) {
            String[][] staff = {{"Meena Devi", "9876500001", "meena@stayease.com"},
                                {"Karthik Raj", "9876500002", "karthik@stayease.com"},
                                {"Anitha S", "9876500003", "anitha@stayease.com"}};
            for (String[] s : staff) {
                Housekeeper h = new Housekeeper();
                h.setName(s[0]);
                h.setPhone(s[1]);
                h.setEmail(s[2]);
                housekeeperRepository.save(h);   // status defaults to AVAILABLE
            }
        }
    }
}
