package com.example.innogeeks.feature_domains.data

import com.example.innogeeks.core.domain.repository.DomainsRepository
import com.example.innogeeks.core.domain.model.Domain
import com.example.innogeeks.core.domain.model.DomainMember
import com.example.innogeeks.core.domain.model.DomainMemberRole.COORDINATOR
import com.example.innogeeks.core.domain.model.DomainMemberRole.TEAM

// Guest-mode data source. No domains endpoint exists yet — swap this for a Ktor-backed
// implementation once /domains ships; DomainsRepository is the contract callers already code
// against. Coordinators and team members below are the real 2026-27 lists.
class InMemoryDomainsRepository : DomainsRepository {

    override suspend fun getDomains(): Result<List<Domain>> {
        return Result.success(
            listOf(
                // App Dev listed first since it's the default selected domain on Home.
                Domain(
                    id = "appd",
                    name = "App Dev",
                    tagline = "Native & cross-platform builders",
                    description = "App Dev designs and ships the club's native and cross-platform mobile apps, end to end.",
                    accentIndex = 1,
                    memberCount = 14,
                    techStack = listOf("Kotlin", "Flutter", "Firebase", "Jetpack Compose"),
                    members = listOf(
                        DomainMember("Srijal Kumar", "SK", COORDINATOR),
                        DomainMember("Aanya Jain", "AJ", COORDINATOR),
                        DomainMember("Daksh Tomar", "DT", COORDINATOR),
                        DomainMember("Divyanshi Bhalla", "DB", COORDINATOR),
                        DomainMember("Atul Kumar Singh", "AS", TEAM),
                        DomainMember("Faiqua Naeem", "FN", TEAM),
                        DomainMember("Dhruv Srivastava", "DS", TEAM)
                    )
                ),
                Domain(
                    id = "webd",
                    name = "Web Dev",
                    tagline = "React, Node & everything between",
                    description = "Web Dev builds and maintains all of Innogeeks' web-facing tools, from the club site to event portals.",
                    accentIndex = 0,
                    memberCount = 18,
                    techStack = listOf("React", "Node.js", "Tailwind", "MongoDB", "TypeScript"),
                    members = listOf(
                        DomainMember("Arjit Mishra", "AM", COORDINATOR),
                        DomainMember("Yashika Kataria", "YK", COORDINATOR),
                        DomainMember("Harsh Dubey", "HD", COORDINATOR),
                        DomainMember("Mohd Mohtashim", "MM", COORDINATOR),
                        DomainMember("Himanshu Yadav", "HY", COORDINATOR),
                        DomainMember("Saumya Sharma", "SS", TEAM),
                        DomainMember("Pratyush Mehra", "PM", TEAM),
                        DomainMember("Harsh Joshi", "HJ", TEAM),
                        DomainMember("Harshit", "HA", TEAM)
                    )
                ),
                Domain(
                    id = "ml",
                    name = "Machine Learning",
                    tagline = "Models, data & leaderboard chasing",
                    description = "Machine Learning explores applied ML and data science, from model training to real-world deployment.",
                    accentIndex = 2,
                    memberCount = 11,
                    techStack = listOf("Python", "TensorFlow", "PyTorch", "Scikit-learn", "Pandas"),
                    members = listOf(
                        DomainMember("Satyam Jaiswal", "SJ", COORDINATOR),
                        DomainMember("Sandhya Singh", "SS", COORDINATOR),
                        DomainMember("Shubhangi Srivastava", "SS", COORDINATOR),
                        DomainMember("Amogh Vatsa", "AV", COORDINATOR),
                        DomainMember("Pranay Jaiswal", "PJ", TEAM),
                        DomainMember("Kanak Verma", "KV", TEAM),
                        DomainMember("Ayush Pandey", "AP", TEAM)
                    )
                ),
                Domain(
                    id = "arvr",
                    name = "AR / VR",
                    tagline = "Spatial experiences & immersive tech",
                    description = "AR / VR prototypes spatial and immersive experiences, working across headsets, mobile AR, and the web.",
                    accentIndex = 3,
                    memberCount = 9,
                    techStack = listOf("Unity", "WebXR", "ARCore", "Blender"),
                    members = listOf(
                        DomainMember("Kumari Alka", "KA", COORDINATOR),
                        DomainMember("Bhaskar Shukla", "BS", COORDINATOR),
                        DomainMember("Aman Chaudhary", "AC", COORDINATOR),
                        DomainMember("Suryansh Patel", "SP", TEAM),
                        DomainMember("Ayush Chaurasia", "AC", TEAM),
                        DomainMember("Ayush Saroj", "AS", TEAM)
                    )
                ),
                Domain(
                    id = "iot",
                    name = "IoT",
                    tagline = "Sensors, boards & the physical world",
                    description = "IoT connects sensors, boards, and the physical world, building hardware-backed projects from prototype to deployment.",
                    accentIndex = 4,
                    memberCount = 8,
                    techStack = listOf("Arduino", "Raspberry Pi", "ESP32", "MQTT"),
                    members = listOf(
                        DomainMember("Shriti Singh", "SS", COORDINATOR),
                        DomainMember("Ayush Pathak", "AP", COORDINATOR),
                        DomainMember("Vansh Baranwal", "VB", COORDINATOR),
                        DomainMember("Abhsihek Patel", "AP", TEAM),
                        DomainMember("Yashi Keservani", "YK", TEAM),
                        DomainMember("Dilip Yadav", "DY", TEAM)
                    )
                )
            )
        )
    }
}
