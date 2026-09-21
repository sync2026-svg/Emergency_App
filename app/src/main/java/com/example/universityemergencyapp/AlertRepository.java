package com.example.universityemergencyapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides sample/dummy alert data so the app is fully functional on the
 * frontend without any backend or network calls.
 */
public class AlertRepository {

    private static List<Alert> sAlerts;

    public static synchronized List<Alert> getAlerts() {
        if (sAlerts == null) {
            sAlerts = new ArrayList<>();
            sAlerts.add(new Alert(
                    "1",
                    "Water outage: Boys Hostel Block C",
                    "Maintenance expected to restore supply by 6 PM today.",
                    "A main supply line to Boys Hostel Block C has developed a leak. " +
                            "Water supply has been shut off for repairs. Maintenance crews are on site " +
                            "and expect to restore supply by 6 PM today. Please use the temporary water " +
                            "points set up near the hostel gate in the meantime.",
                    "10 min ago",
                    Alert.SEVERITY_CRITICAL,
                    "Hostel",
                    "Boys Hostel Block C",
                    "Campus Maintenance"));

            sAlerts.add(new Alert(
                    "2",
                    "Heavy rain warning — Lake Block",
                    "Avoid low-lying pathways near Lake Block until further notice.",
                    "The Meteorological Department has issued a heavy rainfall warning for the " +
                            "district. Pathways near Lake Block are prone to waterlogging. Students and " +
                            "staff are advised to avoid the area and use the alternate route via the " +
                            "Central Library until conditions improve.",
                    "25 min ago",
                    Alert.SEVERITY_WARNING,
                    "Weather",
                    "Lake Block",
                    "Campus Security"));

            sAlerts.add(new Alert(
                    "3",
                    "Scheduled fire drill — Admin Block",
                    "A routine fire drill will be conducted this afternoon at 3 PM.",
                    "As part of the semester safety programme, a fire evacuation drill will be " +
                            "conducted in the Admin Block today at 3 PM. Please follow the instructions " +
                            "of the fire wardens and proceed calmly to the assembly point.",
                    "1 hr ago",
                    Alert.SEVERITY_INFO,
                    "Critical",
                    "Admin Block",
                    "Fire Safety Office"));

            sAlerts.add(new Alert(
                    "4",
                    "Power restored — Girls Hostel Block A",
                    "The earlier power outage has been resolved.",
                    "The power outage reported earlier in Girls Hostel Block A has been resolved. " +
                            "All systems are back to normal operation.",
                    "3 hr ago",
                    Alert.SEVERITY_RESOLVED,
                    "Resolved",
                    "Girls Hostel Block A",
                    "Campus Maintenance"));

            sAlerts.add(new Alert(
                    "5",
                    "Suspicious activity reported near Gate 2",
                    "Campus Security is patrolling the area as a precaution.",
                    "A report of suspicious activity was received near Gate 2 this morning. " +
                            "Campus Security has increased patrols in the area. If you notice anything " +
                            "unusual, please use the Report an Incident feature or call Security directly.",
                    "5 hr ago",
                    Alert.SEVERITY_WARNING,
                    "Critical",
                    "Gate 2",
                    "Campus Security"));

            sAlerts.add(new Alert(
                    "6",
                    "Wi-Fi maintenance — Central Library",
                    "Expect intermittent connectivity between 11 PM and 1 AM.",
                    "The IT department will be carrying out scheduled maintenance on the Wi-Fi " +
                            "network serving the Central Library tonight between 11 PM and 1 AM. " +
                            "Intermittent connectivity may be experienced during this window.",
                    "Yesterday",
                    Alert.SEVERITY_INFO,
                    "Resolved",
                    "Central Library",
                    "IT Services"));
        }
        return sAlerts;
    }

    public static Alert getAlertById(String id) {
        for (Alert alert : getAlerts()) {
            if (alert.getId().equals(id)) {
                return alert;
            }
        }
        return null;
    }
}
