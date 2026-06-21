package com.pokeshophub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate

import org.springframework.beans.factory.annotation.Value
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

@SpringBootApplication
class PokeshopHubApplication {
    @Bean
    fun run(
        jdbcTemplate: JdbcTemplate,
        @Value("\${app.upload.dir}") uploadDir: String
    ): CommandLineRunner {
        return CommandLineRunner {
            try {
                jdbcTemplate.execute("ALTER TABLE interacciones MODIFY cliente_id BIGINT NULL;")
                println("Database migration: cliente_id set to NULLable successfully.")
            } catch (e: Exception) {
                println("Could not alter cliente_id: ${e.message}")
            }
            try {
                jdbcTemplate.execute("ALTER TABLE interacciones MODIFY trabajador_id BIGINT NULL;")
                println("Database migration: trabajador_id set to NULLable successfully.")
            } catch (e: Exception) {
                println("Could not alter trabajador_id: ${e.message}")
            }
            try {
                jdbcTemplate.execute("ALTER TABLE interacciones MODIFY tipo VARCHAR(255);")
                println("Database migration: tipo set to VARCHAR(255) successfully.")
            } catch (e: Exception) {
                println("Could not alter tipo: ${e.message}")
            }
            try {
                val list = jdbcTemplate.queryForList("SELECT id, nombre, imagen_url FROM productos")
                println("PRODUCTS LIST IN DB:")
                for (row in list) {
                    println("ID: ${row["id"]}, Name: ${row["nombre"]}, ImageUrl: ${row["imagen_url"]}")
                }
            } catch (e: Exception) {
                println("Could not list products: ${e.message}")
            }
            try {
                val uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize()
                Files.createDirectories(uploadPath)
                val defaultImages = listOf(
                    "accessories.png",
                    "booster_box.png",
                    "booster_pack.png",
                    "card_illustration_rare.png",
                    "card_ultra_rare.png"
                )
                for (imageName in defaultImages) {
                    val targetFile = uploadPath.resolve(imageName)
                    if (!Files.exists(targetFile)) {
                        val resourceStream = PokeshopHubApplication::class.java.getResourceAsStream("/default-uploads/$imageName")
                        if (resourceStream != null) {
                            Files.copy(resourceStream, targetFile)
                            println("Default image copied successfully to uploads: $imageName")
                        } else {
                            println("Default image resource not found in classpath: /default-uploads/$imageName")
                        }
                    } else {
                        println("Default image already exists in uploads: $imageName")
                    }
                }
            } catch (e: Exception) {
                println("Error initializing default images: ${e.message}")
            }
        }
    }
}


fun main(args: Array<String>) {
    runApplication<PokeshopHubApplication>(*args)
}
