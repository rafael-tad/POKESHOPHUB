package com.pokeshophub

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootApplication
class PokeshopHubApplication {
    @Bean
    fun run(jdbcTemplate: JdbcTemplate): CommandLineRunner {
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
        }
    }
}


fun main(args: Array<String>) {
    runApplication<PokeshopHubApplication>(*args)
}
