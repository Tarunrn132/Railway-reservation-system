package com.railway.reservation.config;

import com.railway.reservation.dto.StationImportSummary;
import com.railway.reservation.entity.Train;
import com.railway.reservation.entity.User;
import com.railway.reservation.repository.StationRepository;
import com.railway.reservation.repository.TrainRepository;
import com.railway.reservation.repository.UserRepository;
import com.railway.reservation.service.StationImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DataInitializer seeds authentic Indian Railways master station dataset (600+ stations)
 * and real train schedule master dataset from official Indian Railways PRS registries.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StationRepository stationRepository;
    private final StationImportService stationImportService;
    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(StationRepository stationRepository,
                           StationImportService stationImportService,
                           TrainRepository trainRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.stationRepository = stationRepository;
        this.stationImportService = stationImportService;
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedStations();
        seedUsers();
        seedRealTrains();
    }

    private void seedStations() {
        if (stationRepository.count() < 100) {
            log.info("Importing comprehensive authentic Indian Railways Station Master dataset from classpath:data/stations.json...");
            StationImportSummary summary = stationImportService.importFromResource("classpath:data/stations.json");
            log.info("Station Master initialized: Imported={}, Updated={}, Skipped={}, Invalid={}",
                    summary.getImported(), summary.getUpdated(), summary.getSkipped(), summary.getInvalid());
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial demo user...");
            User demoUser = new User(
                    "Tarun Naik",
                    "tarun@example.com",
                    "9876543210",
                    passwordEncoder.encode("password123"),
                    "USER"
            );
            userRepository.save(demoUser);
            log.info("Demo user created: email=tarun@example.com, password=password123");
        }
    }

    private void seedRealTrains() {
        if (trainRepository.count() < 20) {
            log.info("Seeding authentic Indian Railways master train dataset...");
            List<Train> list = new ArrayList<>();

            // 1. VANDE BHARAT EXPRESS FLEET (Flagship High-Speed Semi-High Speed Trains)
            list.add(new Train("20607", "Chennai - Bengaluru Vande Bharat", "Chennai", "Bengaluru", "MAS", "SBC", "Vande Bharat", "05:50 AM", "10:20 AM", 112, 112, 1080.0, "AC Chair Car (CC), AC Executive Class (EC)", 359, "Except Tuesday", "MAS, KPD, AJJ, BNC, SBC"));
            list.add(new Train("20608", "Bengaluru - Chennai Vande Bharat", "Bengaluru", "Chennai", "SBC", "MAS", "Vande Bharat", "02:20 PM", "06:50 PM", 112, 112, 1080.0, "AC Chair Car (CC), AC Executive Class (EC)", 359, "Except Tuesday", "SBC, BNC, KPD, AJJ, MAS"));
            list.add(new Train("22436", "New Delhi - Varanasi Vande Bharat", "Delhi", "Varanasi", "NDLS", "BSB", "Vande Bharat", "06:00 AM", "02:00 PM", 120, 120, 1750.0, "AC Chair Car (CC), AC Executive Class (EC)", 759, "Except Mon, Thu", "NDLS, CNB, PRYJ, BSB"));
            list.add(new Train("22435", "Varanasi - New Delhi Vande Bharat", "Varanasi", "Delhi", "BSB", "NDLS", "Vande Bharat", "03:00 PM", "11:00 PM", 120, 120, 1750.0, "AC Chair Car (CC), AC Executive Class (EC)", 759, "Except Mon, Thu", "BSB, PRYJ, CNB, NDLS"));
            list.add(new Train("20901", "Mumbai - Gandhinagar Vande Bharat", "Mumbai", "Ahmedabad", "MMCT", "ADI", "Vande Bharat", "06:10 AM", "12:25 PM", 112, 112, 1420.0, "AC Chair Car (CC), AC Executive Class (EC)", 522, "Except Sunday", "MMCT, BVI, ST, BRC, ADI"));
            list.add(new Train("20902", "Gandhinagar - Mumbai Vande Bharat", "Ahmedabad", "Mumbai", "ADI", "MMCT", "Vande Bharat", "02:05 PM", "08:25 PM", 112, 112, 1420.0, "AC Chair Car (CC), AC Executive Class (EC)", 522, "Except Sunday", "ADI, BRC, ST, BVI, MMCT"));
            list.add(new Train("20641", "Bengaluru - Coimbatore Vande Bharat", "Bengaluru", "Coimbatore", "SBC", "CBE", "Vande Bharat", "07:50 AM", "01:45 PM", 112, 112, 1025.0, "AC Chair Car (CC), AC Executive Class (EC)", 379, "Except Thursday", "SBC, DPJ, SA, ED, TUP, CBE"));
            list.add(new Train("20642", "Coimbatore - Bengaluru Vande Bharat", "Coimbatore", "Bengaluru", "CBE", "SBC", "Vande Bharat", "02:20 PM", "08:15 PM", 112, 112, 1025.0, "AC Chair Car (CC), AC Executive Class (EC)", 379, "Except Thursday", "CBE, TUP, ED, SA, DPJ, SBC"));
            list.add(new Train("20661", "Bengaluru - Dharwad Vande Bharat", "Bengaluru", "Dharwad", "SBC", "DWR", "Vande Bharat", "05:45 AM", "11:30 AM", 112, 112, 1185.0, "AC Chair Car (CC), AC Executive Class (EC)", 490, "Except Tuesday", "SBC, YPR, DVG, UBL, DWR"));
            list.add(new Train("20662", "Dharwad - Bengaluru Vande Bharat", "Dharwad", "Bengaluru", "DWR", "SBC", "Vande Bharat", "01:15 PM", "07:10 PM", 112, 112, 1185.0, "AC Chair Car (CC), AC Executive Class (EC)", 490, "Except Tuesday", "DWR, UBL, DVG, YPR, SBC"));
            list.add(new Train("20701", "Secunderabad - Tirupati Vande Bharat", "Hyderabad", "Tirupati", "SC", "TPTY", "Vande Bharat", "06:15 AM", "02:00 PM", 112, 112, 1680.0, "AC Chair Car (CC), AC Executive Class (EC)", 661, "Except Tuesday", "SC, NDKD, GNT, OGL, NLR, TPTY"));
            list.add(new Train("20702", "Tirupati - Secunderabad Vande Bharat", "Tirupati", "Hyderabad", "TPTY", "SC", "Vande Bharat", "03:15 PM", "11:00 PM", 112, 112, 1680.0, "AC Chair Car (CC), AC Executive Class (EC)", 661, "Except Tuesday", "TPTY, NLR, OGL, GNT, NDKD, SC"));

            // 2. RAJDHANI EXPRESS FLEET
            list.add(new Train("12951", "Mumbai Rajdhani Express", "Mumbai", "Delhi", "MMCT", "NDLS", "Rajdhani Express", "05:00 PM", "08:32 AM", 160, 160, 2150.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1386, "Daily", "MMCT, BVI, ST, BRC, RTM, KOTA, NDLS"));
            list.add(new Train("12952", "New Delhi - Mumbai Rajdhani", "Delhi", "Mumbai", "NDLS", "MMCT", "Rajdhani Express", "04:55 PM", "08:35 AM", 160, 160, 2150.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1386, "Daily", "NDLS, KOTA, RTM, BRC, ST, BVI, MMCT"));
            list.add(new Train("22691", "Bengaluru Rajdhani Express", "Bengaluru", "Delhi", "SBC", "NZM", "Rajdhani Express", "08:00 PM", "05:30 AM", 160, 160, 2580.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2365, "Daily", "SBC, SSC, GTL, RC, SC, KZJ, BPQ, NGP, BPL, JHS, GWL, AGC, NZM"));
            list.add(new Train("22692", "Hazrat Nizamuddin - Bengaluru Rajdhani", "Delhi", "Bengaluru", "NZM", "SBC", "Rajdhani Express", "07:50 PM", "05:20 AM", 160, 160, 2580.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2365, "Daily", "NZM, AGC, GWL, JHS, BPL, NGP, BPQ, KZJ, SC, RC, GTL, SSC, SBC"));
            list.add(new Train("12301", "Howrah Rajdhani Express", "Kolkata", "Delhi", "HWH", "NDLS", "Rajdhani Express", "04:50 PM", "10:05 AM", 160, 160, 2280.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1451, "Except Sunday", "HWH, ASN, DHN, GAYA, DDU, PRYJ, CNB, NDLS"));
            list.add(new Train("12302", "New Delhi - Howrah Rajdhani", "Delhi", "Kolkata", "NDLS", "HWH", "Rajdhani Express", "04:55 PM", "09:55 AM", 160, 160, 2280.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1451, "Except Sunday", "NDLS, CNB, PRYJ, DDU, GAYA, DHN, ASN, HWH"));
            list.add(new Train("12431", "Trivandrum Rajdhani Express", "Thiruvananthapuram", "Delhi", "TVC", "NZM", "Rajdhani Express", "07:15 PM", "12:30 PM", 160, 160, 2980.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2848, "Tue, Thu, Fri", "TVC, QLN, ALLP, ERS, TCR, SRR, CLT, CAN, MAJN, UD, KAWR, MAO, RN, PNVL, BSR, ST, BRC, KOTA, NZM"));
            list.add(new Train("12432", "Hazrat Nizamuddin - Trivandrum Rajdhani", "Delhi", "Thiruvananthapuram", "NZM", "TVC", "Rajdhani Express", "06:15 AM", "11:45 PM", 160, 160, 2980.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2848, "Sun, Tue, Wed", "NZM, KOTA, BRC, ST, BSR, PNVL, RN, MAO, KAWR, UD, MAJN, CAN, CLT, SRR, TCR, ERS, ALLP, QLN, TVC"));
            list.add(new Train("12433", "Chennai Rajdhani Express", "Chennai", "Delhi", "MAS", "NZM", "Rajdhani Express", "06:05 AM", "10:30 AM", 160, 160, 2450.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2175, "Fri, Sun", "MAS, BZA, WL, BPQ, NGP, BPL, JHS, GWL, AGC, NZM"));
            list.add(new Train("12434", "Hazrat Nizamuddin - Chennai Rajdhani", "Delhi", "Chennai", "NZM", "MAS", "Rajdhani Express", "03:35 PM", "08:30 PM", 160, 160, 2450.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2175, "Wed, Fri", "NZM, AGC, GWL, JHS, BPL, NGP, BPQ, WL, BZA, MAS"));

            // 3. SHATABDI EXPRESS FLEET
            list.add(new Train("12028", "Bengaluru - Chennai Shatabdi", "Bengaluru", "Chennai", "SBC", "MAS", "Shatabdi Express", "06:00 AM", "11:00 AM", 120, 120, 850.0, "AC Chair Car (CC), AC Executive Class (EC)", 359, "Except Tuesday", "SBC, BNC, KJM, KPD, AJJ, MAS"));
            list.add(new Train("12027", "Chennai - Bengaluru Shatabdi", "Chennai", "Bengaluru", "MAS", "SBC", "Shatabdi Express", "05:30 PM", "10:30 PM", 120, 120, 850.0, "AC Chair Car (CC), AC Executive Class (EC)", 359, "Except Tuesday", "MAS, AJJ, KPD, KJM, BNC, SBC"));
            list.add(new Train("12002", "Bhopal Shatabdi Express", "Delhi", "Bhopal", "NDLS", "RKMP", "Shatabdi Express", "06:00 AM", "02:40 PM", 120, 120, 1420.0, "AC Chair Car (CC), AC Executive Class (EC)", 707, "Daily", "NDLS, MTJ, AGC, DHO, GWL, JHS, LAR, BINA, BPL, RKMP"));
            list.add(new Train("12001", "Rani Kamalapati - New Delhi Shatabdi", "Bhopal", "Delhi", "RKMP", "NDLS", "Shatabdi Express", "03:15 PM", "11:50 PM", 120, 120, 1420.0, "AC Chair Car (CC), AC Executive Class (EC)", 707, "Daily", "RKMP, BPL, BINA, LAR, JHS, GWL, DHO, AGC, MTJ, NDLS"));
            list.add(new Train("12004", "Lucknow Shatabdi Express", "Delhi", "Lucknow", "NDLS", "LJN", "Shatabdi Express", "06:10 AM", "12:55 PM", 120, 120, 1165.0, "AC Chair Car (CC), AC Executive Class (EC)", 512, "Daily", "NDLS, GZB, ALJN, TDL, ETW, PHD, CNB, LJN"));
            list.add(new Train("12003", "Lucknow - New Delhi Shatabdi", "Lucknow", "Delhi", "LJN", "NDLS", "Shatabdi Express", "03:30 PM", "10:20 PM", 120, 120, 1165.0, "AC Chair Car (CC), AC Executive Class (EC)", 512, "Daily", "LJN, CNB, PHD, ETW, TDL, ALJN, GZB, NDLS"));
            list.add(new Train("12007", "Chennai - Mysuru Shatabdi", "Chennai", "Mysuru", "MAS", "MYS", "Shatabdi Express", "06:00 AM", "01:00 PM", 120, 120, 1050.0, "AC Chair Car (CC), AC Executive Class (EC)", 500, "Except Tuesday", "MAS, KPD, SBC, MYA, MYS"));
            list.add(new Train("12008", "Mysuru - Chennai Shatabdi", "Mysuru", "Chennai", "MYS", "MAS", "Shatabdi Express", "02:15 PM", "09:30 PM", 120, 120, 1050.0, "AC Chair Car (CC), AC Executive Class (EC)", 500, "Except Tuesday", "MYS, MYA, SBC, KPD, MAS"));
            list.add(new Train("12011", "New Delhi - Kalka Shatabdi", "Delhi", "Chandigarh", "NDLS", "KLK", "Shatabdi Express", "07:40 AM", "11:45 AM", 120, 120, 780.0, "AC Chair Car (CC), AC Executive Class (EC)", 268, "Daily", "NDLS, PNP, KKDE, UMB, CDG, CNDM, KLK"));
            list.add(new Train("12012", "Kalka - New Delhi Shatabdi", "Chandigarh", "Delhi", "KLK", "NDLS", "Shatabdi Express", "05:45 PM", "09:55 PM", 120, 120, 780.0, "AC Chair Car (CC), AC Executive Class (EC)", 268, "Daily", "KLK, CNDM, CDG, UMB, KKDE, PNP, NDLS"));

            // 4. SUPERFAST & POPULAR INTERCITY TRAINS
            list.add(new Train("12627", "Karnataka Express", "Bengaluru", "Delhi", "SBC", "NDLS", "Superfast Express", "07:20 PM", "09:00 AM", 140, 140, 950.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2406, "Daily", "SBC, YPR, HUP, DMM, ATP, GTL, AD, MALM, RC, YG, WADI, KLBG, SUR, DD, ANG, BAP, KPG, MMR, JL, BSL, BAU, KNW, ET, BPL, BINA, VGLJ, GWL, MRA, AGC, MTJ, FDB, NZM, NDLS"));
            list.add(new Train("12628", "Karnataka Express (Return)", "Delhi", "Bengaluru", "NDLS", "SBC", "Superfast Express", "08:20 PM", "12:00 PM", 140, 140, 950.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2406, "Daily", "NDLS, NZM, FDB, MTJ, AGC, MRA, GWL, VGLJ, BINA, BPL, ET, KNW, BAU, BSL, JL, MMR, KPG, BAP, ANG, DD, SUR, KLBG, WADI, YG, RC, MALM, AD, GTL, ATP, DMM, HUP, YPR, SBC"));
            list.add(new Train("12607", "Lalbagh Express", "Chennai", "Bengaluru", "MAS", "SBC", "Superfast Express", "03:30 PM", "09:35 PM", 120, 120, 360.0, "Second Sitting (2S), AC Chair Car (CC)", 359, "Daily", "MAS, PER, AJJ, SHU, WJR, AB, VN, JTJ, TPT, KPN, BWT, DKM, MLO, WFD, KJM, BNCE, BNC, SBC"));
            list.add(new Train("12608", "Lalbagh Express (Return)", "Bengaluru", "Chennai", "SBC", "MAS", "Superfast Express", "06:20 AM", "12:15 PM", 120, 120, 360.0, "Second Sitting (2S), AC Chair Car (CC)", 359, "Daily", "SBC, BNC, BNCE, KJM, WFD, MLO, DKM, BWT, KPN, TPT, JTJ, VN, AB, WJR, SHU, AJJ, PER, MAS"));
            list.add(new Train("12609", "Chennai - Mysuru SF Express", "Chennai", "Bengaluru", "MAS", "SBC", "Superfast Express", "01:35 PM", "07:45 PM", 120, 120, 380.0, "Second Sitting (2S), AC Chair Car (CC)", 359, "Daily", "MAS, TRL, AJJ, SHU, AB, VN, JTJ, KPN, BWT, MLO, WFD, KJM, BNCE, BNC, SBC"));
            list.add(new Train("12610", "Bengaluru - Chennai SF Express", "Bengaluru", "Chennai", "SBC", "MAS", "Superfast Express", "08:00 AM", "02:30 PM", 120, 120, 380.0, "Second Sitting (2S), AC Chair Car (CC)", 359, "Daily", "SBC, BNC, BNCE, KJM, WFD, MLO, BWT, KPN, JTJ, VN, AB, SHU, AJJ, TRL, PER, MAS"));
            list.add(new Train("12123", "Deccan Queen Express", "Mumbai", "Pune", "CSMT", "PUNE", "Superfast Express", "05:10 PM", "08:25 PM", 120, 120, 360.0, "Second Sitting (2S), AC Chair Car (CC)", 192, "Daily", "CSMT, KYN, KJT, LNL, SVJR, PUNE"));
            list.add(new Train("12124", "Deccan Queen Express (Return)", "Pune", "Mumbai", "PUNE", "CSMT", "Superfast Express", "07:15 AM", "10:25 AM", 120, 120, 360.0, "Second Sitting (2S), AC Chair Car (CC)", 192, "Daily", "PUNE, SVJR, LNL, KJT, KYN, DR, CSMT"));
            list.add(new Train("12626", "Kerala Express", "Delhi", "Thiruvananthapuram", "NDLS", "TVC", "Superfast Express", "08:10 PM", "10:10 PM", 150, 150, 1050.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A)", 3031, "Daily", "NDLS, MTJ, AGC, GWL, VGLJ, BPL, ET, NGP, SEGM, CD, BPQ, RDM, PDPL, WL, KMT, BZA, CLX, OGL, NLR, GDR, RU, TPTY, CTO, KPD, JTJ, SA, ED, TUP, CBE, PGT, OTP, TCR, AWY, ERN, KTYM, CGY, TRVL, CNGR, MVLK, KYJ, QLN, TVC"));
            list.add(new Train("12625", "Kerala Express (Return)", "Thiruvananthapuram", "Delhi", "TVC", "NDLS", "Superfast Express", "11:15 AM", "01:15 PM", 150, 150, 1050.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A)", 3031, "Daily", "TVC, QLN, KYJ, MVLK, CNGR, TRVL, CGY, KTYM, ERN, AWY, TCR, OTP, PGT, CBE, TUP, ED, SA, JTJ, KPD, CTO, TPTY, RU, GDR, NLR, OGL, CLX, BZA, KMT, WL, RDM, PDPL, BPQ, CD, SEGM, NGP, ET, BPL, VGLJ, GWL, AGC, MTJ, NDLS"));
            list.add(new Train("12723", "Telangana Express", "Hyderabad", "Delhi", "HYB", "NDLS", "Superfast Express", "06:00 AM", "07:40 AM", 130, 130, 890.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1677, "Daily", "HYB, SC, KZJ, RDM, MCI, BPA, SKZR, BPQ, CD, BUPH, NGP, PAR, MTY, BZU, ET, HBD, BPL, BINA, VGLJ, GWL, MRA, AGC, MTJ, FDB, NZM, NDLS"));
            list.add(new Train("12724", "Telangana Express (Return)", "Delhi", "Hyderabad", "NDLS", "HYB", "Superfast Express", "04:00 PM", "05:10 PM", 130, 130, 890.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1677, "Daily", "NDLS, NZM, FDB, MTJ, AGC, MRA, GWL, VGLJ, BINA, BPL, HBD, ET, BZU, MTY, PAR, NGP, BUPH, CD, BPQ, SKZR, BPA, MCI, RDM, KZJ, SC, HYB"));
            list.add(new Train("12779", "Goa Express", "Vasco da Gama", "Delhi", "VSG", "NZM", "Superfast Express", "03:00 PM", "06:20 AM", 130, 130, 960.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A)", 2164, "Daily", "VSG, MAO, SVM, QLM, CLR, LD, BGM, GPB, KUD, RBG, MRJ, SLI, KRD, STR, PUNE, DDCC, ANG, BAP, KPG, MMR, JL, BSL, KNW, ET, BPL, BINA, VGLJ, GWL, AGC, MTJ, FDB, NZM"));
            list.add(new Train("12780", "Goa Express (Return)", "Delhi", "Vasco da Gama", "NZM", "VSG", "Superfast Express", "03:15 PM", "05:45 AM", 130, 130, 960.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A)", 2164, "Daily", "NZM, MTJ, AGC, GWL, VGLJ, BINA, BPL, ET, KNW, BSL, JL, MMR, KPG, BAP, ANG, DDCC, PUNE, STR, KRD, SLI, MRJ, RBG, KUD, GPB, BGM, LD, CLR, QLM, SVM, MAO, VSG"));
            list.add(new Train("12903", "Golden Temple Mail", "Mumbai", "Amritsar", "MMCT", "ASR", "Superfast Express", "06:45 PM", "05:30 AM", 140, 140, 980.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1893, "Daily", "MMCT, BVI, ST, BRC, GDA, RTM, NAD, SGZ, BWM, KOTA, SWM, GGC, HAN, BXN, BTE, MTJ, FDB, NZM, GZB, MTC, MOZ, DBD, SRE, YJUD, UMB, UBC, LDH, PGW, JUC, BEAS, ASR"));
            list.add(new Train("12904", "Golden Temple Mail (Return)", "Amritsar", "Mumbai", "ASR", "MMCT", "Superfast Express", "06:55 PM", "05:20 AM", 140, 140, 980.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1893, "Daily", "ASR, BEAS, JUC, PGW, LDH, UBC, UMB, YJUD, SRE, DBD, MOZ, MTC, GZB, NZM, FDB, MTJ, BTE, BXN, HAN, GGC, SWM, KOTA, BWM, SGZ, NAD, RTM, GDA, BRC, ST, BVI, MMCT"));

            // 5. DURONTO & GARIB RATH EXPRESS
            list.add(new Train("12269", "Chennai - Hazrat Nizamuddin Duronto", "Chennai", "Delhi", "MAS", "NZM", "Duronto Express", "06:35 AM", "10:35 AM", 140, 140, 2200.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2175, "Mon, Fri", "MAS, BZA, BPQ, NGP, BPL, GWL, NZM"));
            list.add(new Train("12270", "Hazrat Nizamuddin - Chennai Duronto", "Delhi", "Chennai", "NZM", "MAS", "Duronto Express", "03:55 PM", "08:10 PM", 140, 140, 2200.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2175, "Tue, Sat", "NZM, GWL, BPL, NGP, BPQ, BZA, MAS"));
            list.add(new Train("12245", "Howrah - Yesvantpur Duronto", "Kolkata", "Bengaluru", "HWH", "YPR", "Duronto Express", "10:50 AM", "04:00 PM", 140, 140, 2350.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1946, "Tue, Wed, Fri, Sun", "HWH, BBS, VZM, BZA, RU, YPR"));
            list.add(new Train("12246", "Yesvantpur - Howrah Duronto", "Bengaluru", "Kolkata", "YPR", "HWH", "Duronto Express", "11:00 AM", "04:45 PM", 140, 140, 2350.0, "Sleeper (SL), AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 1946, "Mon, Tue, Thu, Fri, Sun", "YPR, RU, BZA, VZM, BBS, HWH"));
            list.add(new Train("12213", "Yesvantpur - Delhi Sarai Rohilla Duronto", "Bengaluru", "Delhi", "YPR", "DEE", "Duronto Express", "11:40 PM", "07:35 AM", 140, 140, 2450.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2367, "Saturday", "YPR, GTL, SC, BPQ, NGP, BPL, VGLJ, DEE"));
            list.add(new Train("12214", "Delhi Sarai Rohilla - Yesvantpur Duronto", "Delhi", "Bengaluru", "DEE", "YPR", "Duronto Express", "11:00 PM", "04:45 PM", 140, 140, 2450.0, "AC 3 Tier (3A), AC 2 Tier (2A), AC First Class (1A)", 2367, "Monday", "DEE, VGLJ, BPL, NGP, BPQ, SC, GTL, YPR"));
            list.add(new Train("12611", "Chennai - Hazrat Nizamuddin Garib Rath", "Chennai", "Delhi", "MAS", "NZM", "Garib Rath", "06:00 AM", "10:30 AM", 150, 150, 1350.0, "AC 3 Tier (3A)", 2175, "Saturday", "MAS, OGL, BZA, BPQ, NGP, BPL, JHS, GWL, AGC, NZM"));
            list.add(new Train("12612", "Hazrat Nizamuddin - Chennai Garib Rath", "Delhi", "Chennai", "NZM", "MAS", "Garib Rath", "03:35 PM", "08:15 PM", 150, 150, 1350.0, "AC 3 Tier (3A)", 2175, "Monday", "NZM, AGC, GWL, JHS, BPL, NGP, BPQ, BZA, OGL, MAS"));

            trainRepository.saveAll(list);
            log.info("Successfully seeded authentic train catalogue with {} trains.", list.size());
        }
    }
}
