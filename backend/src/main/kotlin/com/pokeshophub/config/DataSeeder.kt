package com.pokeshophub.config

import com.pokeshophub.model.*
import com.pokeshophub.repository.*
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

@Configuration
class DataSeeder(
    private val trabajadorRepository: TrabajadorRepository,
    private val clienteRepository: ClienteRepository,
    private val productoRepository: ProductoRepository,
    private val torneoRepository: TorneoRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun seedData(): CommandLineRunner {
        return CommandLineRunner {
            if (trabajadorRepository.count() == 0L) {
                println(">>> POKESHOP HUB: INYECTANDO DATOS DE PRUEBA... <<<")

                // Crear Administrador / Staff
                val admin = Trabajador(
                    nombre = "Staff PokeShop",
                    apellidos = "Admin",
                    email = "admin@pokeshophub.com",
                    password = passwordEncoder.encode("admin"),
                    rol = Rol.ADMIN
                )
                trabajadorRepository.save(admin)

                // Crear Cliente 1 con saldo
                val cliente1 = Cliente(
                    nombre = "Juan TCG",
                    apellidos = "Pérez",
                    dni = "12345678A",
                    email = "juan@cliente.com",
                    telefono = "600100200",
                    password = passwordEncoder.encode("juan123"),
                    saldo = 50.0 // Billetera inicial
                )
                clienteRepository.save(cliente1)

                // Crear Cliente 2 con saldo
                val cliente2 = Cliente(
                    nombre = "María Coleccionista",
                    apellidos = "López",
                    dni = "87654321B",
                    email = "maria@cliente.com",
                    telefono = "600300400",
                    password = passwordEncoder.encode("maria123"),
                    saldo = 120.0
                )
                clienteRepository.save(cliente2)

                // Semilla de Torneos
                torneoRepository.saveAll(listOf(
                    Torneo(
                        nombre = "Torneo de Presentación: Escarlata y Púrpura",
                        descripcion = "Torneo formato pre-release. Se entrega kit de presentación a cada jugador.",
                        fecha = LocalDate.now().plusDays(3),
                        hora = LocalTime.of(17, 0),
                        maxParticipantes = 16,
                        participantesActuales = 0,
                        precioInscripcion = 25.0,
                        estado = "ABIERTO"
                    ),
                    Torneo(
                        nombre = "Liga Semanal PokeShop (Estándar)",
                        descripcion = "Ven con tu mazo estándar y compite por sobres oficiales de premio.",
                        fecha = LocalDate.now().plusDays(7),
                        hora = LocalTime.of(10, 30),
                        maxParticipantes = 32,
                        participantesActuales = 0,
                        precioInscripcion = 5.0,
                        estado = "ABIERTO"
                    )
                ))

                println(">>> DATOS DE PRUEBA INYECTADOS CON ÉXITO EN POKESHOP HUB <<<")
            }

            // Inyección automática del nuevo catálogo de productos reales 2026
            if (productoRepository.count() < 20L) {
                productoRepository.deleteAll()
                productoRepository.saveAll(listOf(
                    // Sobres
                    Producto(
                        nombre = "Sobre Pokémon - Mega Evolution: Ascended Heroes",
                        descripcion = "Sobre de 10 cartas adicionales de la expansión Ascended Heroes (Ene 2026).",
                        precio = 4.99,
                        stock = 120,
                        imagenUrl = "booster_pack.png",
                        categoria = "Sobres"
                    ),
                    Producto(
                        nombre = "Sobre Pokémon - Mega Evolution: Perfect Order",
                        descripcion = "Sobre de 10 cartas adicionales de la expansión Perfect Order (Mar 2026).",
                        precio = 4.99,
                        stock = 150,
                        imagenUrl = "booster_pack.png",
                        categoria = "Sobres"
                    ),
                    Producto(
                        nombre = "Sobre Pokémon - Mega Evolution: Chaos Rising",
                        descripcion = "Sobre de 10 cartas adicionales de la expansión más reciente Chaos Rising (May 2026).",
                        precio = 4.99,
                        stock = 200,
                        imagenUrl = "booster_pack.png",
                        categoria = "Sobres"
                    ),
                    Producto(
                        nombre = "Sobre Pokémon - Mega Evolution: Pitch Black (Pre-compra)",
                        descripcion = "Reserva ya tu sobre de la próxima expansión Pitch Black, lanzamiento Julio 2026.",
                        precio = 5.49,
                        stock = 100,
                        imagenUrl = "booster_pack.png",
                        categoria = "Sobres"
                    ),
                    Producto(
                        nombre = "Pack de 3 Sobres - Mega Evolution: Chaos Rising",
                        descripcion = "Blíster oficial que incluye 3 sobres de Chaos Rising y una carta promo holográfica de Mew.",
                        precio = 14.99,
                        stock = 45,
                        imagenUrl = "booster_pack.png",
                        categoria = "Sobres"
                    ),

                    // Cajas
                    Producto(
                        nombre = "Elite Trainer Box (ETB) - Mega Evolution: Ascended Heroes",
                        descripcion = "Contiene 9 sobres de Ascended Heroes, 65 fundas exclusivas, dados, marcadores y guía de juego.",
                        precio = 54.99,
                        stock = 15,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Elite Trainer Box (ETB) - Mega Evolution: Perfect Order",
                        descripcion = "Contiene 9 sobres de Perfect Order, 65 fundas oficiales, accesorios premium y guía coleccionista.",
                        precio = 54.99,
                        stock = 12,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Elite Trainer Box (ETB) - Mega Evolution: Chaos Rising",
                        descripcion = "Caja de entrenador élite de la última colección Chaos Rising (Lanzamiento Mayo 2026).",
                        precio = 54.99,
                        stock = 24,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Booster Box (Caja de 36 sobres) - Mega Evolution: Chaos Rising",
                        descripcion = "Caja sellada que contiene 36 sobres oficiales de la expansión Chaos Rising. Ideal para apertura masiva.",
                        precio = 145.00,
                        stock = 8,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Booster Box (Caja de 36 sobres) - Mega Evolution: Perfect Order",
                        descripcion = "Caja de 36 sobres sellada de la expansión Perfect Order. Garantiza altas probabilidades de rarezas.",
                        precio = 145.00,
                        stock = 6,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Premium Collection Box - Mega Charizard X & Y",
                        descripcion = "Colección especial que incluye una carta gigante de Mega Charizard y 6 sobres variados.",
                        precio = 39.99,
                        stock = 10,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Baraja de Combate Deluxe - Mega Rayquaza ex",
                        descripcion = "Mazo preconstruido de 60 cartas listo para jugar a nivel competitivo, centrado en Mega Rayquaza.",
                        precio = 22.99,
                        stock = 20,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),
                    Producto(
                        nombre = "Baraja de Combate Deluxe - Mega Mewtwo Y ex",
                        descripcion = "Mazo preconstruido oficial de 60 cartas con estrategias listas para torneos.",
                        precio = 22.99,
                        stock = 18,
                        imagenUrl = "booster_box.png",
                        categoria = "Cajas"
                    ),

                    // Cartas sueltas
                    Producto(
                        nombre = "Mega Charizard Y ex (Ultra Rara - 089/108)",
                        descripcion = "Carta suelta de Mega Charizard Y ex de la colección Ascended Heroes. Estado impecable (Near Mint/Mint).",
                        precio = 185.00,
                        stock = 1,
                        imagenUrl = "card_ultra_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Mega Rayquaza ex (Ilustración Especial - 122/108)",
                        descripcion = "Carta ultra rara con ilustración especial y arte alternativo de Chaos Rising. Joya de coleccionista.",
                        precio = 295.00,
                        stock = 1,
                        imagenUrl = "card_illustration_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Mega Mewtwo X ex (Secreta Rara - 115/108)",
                        descripcion = "Mewtwo en su versión Mega Evolución X, carta dorada secreta de Perfect Order. Estado impecable.",
                        precio = 120.00,
                        stock = 1,
                        imagenUrl = "card_ultra_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Pikachu ex (Ilustración Especial Rara - 105/108)",
                        descripcion = "Preciosa carta con arte alternativo de Pikachu rodeado de destellos eléctricos de Chaos Rising.",
                        precio = 150.00,
                        stock = 2,
                        imagenUrl = "card_illustration_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Mew ex (Rara Ilustración Especial - 101/108)",
                        descripcion = "Carta legendaria de Mew ex con arte alternativo pastel de la expansión Ascended Heroes.",
                        precio = 110.00,
                        stock = 1,
                        imagenUrl = "card_illustration_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Gengar ex (Ultra Rara - 045/108)",
                        descripcion = "Gengar ex con arte dinámico de la colección Perfect Order. Ideal para mazos de tipo Psíquico.",
                        precio = 45.00,
                        stock = 3,
                        imagenUrl = "card_ultra_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Lucario ex (Holográfica - 021/108)",
                        descripcion = "Lucario ex de tipo Lucha procedente de Chaos Rising. Excelente estado de conservación.",
                        precio = 18.50,
                        stock = 5,
                        imagenUrl = "card_ultra_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Gardevoir ex (Ilustración Especial - 110/108)",
                        descripcion = "Gardevoir ex con ilustración especial muy cotizada de la colección Perfect Order.",
                        precio = 85.00,
                        stock = 1,
                        imagenUrl = "card_illustration_rare.png",
                        categoria = "Cartas"
                    ),
                    Producto(
                        nombre = "Dragonite ex (Ultra Rara - 074/108)",
                        descripcion = "Carta de Dragonite ex de tipo Dragón de la colección Ascended Heroes.",
                        precio = 35.00,
                        stock = 4,
                        imagenUrl = "card_ultra_rare.png",
                        categoria = "Cartas"
                    ),

                    // Accesorios
                    Producto(
                        nombre = "Archivador Premium de 9 Bolsillos - Mega Rayquaza",
                        descripcion = "Archivador oficial con páginas acolchadas libres de ácido para proteger y lucir tus mejores cartas.",
                        precio = 24.99,
                        stock = 15,
                        imagenUrl = "accessories.png",
                        categoria = "Accesorios"
                    ),
                    Producto(
                        nombre = "Fundas Protectoras Oficiales (65 uds) - Mega Charizard Y",
                        descripcion = "Paquete de 65 fundas mate de alta calidad para cartas de tamaño estándar con ilustración trasera.",
                        precio = 9.99,
                        stock = 50,
                        imagenUrl = "accessories.png",
                        categoria = "Accesorios"
                    ),
                    Producto(
                        nombre = "Caja Porta-Mazos (Deck Box) de Cuero - PokeShop Hub Edition",
                        descripcion = "Deck Box premium fabricada en cuero sintético con cierre magnético y capacidad para 100 cartas.",
                        precio = 14.99,
                        stock = 25,
                        imagenUrl = "accessories.png",
                        categoria = "Accesorios"
                    ),
                    Producto(
                        nombre = "Tapete de Juego Oficial (Playmat) - Mega Mewtwo",
                        descripcion = "Tapete de tela suave con base de goma antideslizante para jugar cómodamente tus partidas.",
                        precio = 21.99,
                        stock = 15,
                        imagenUrl = "accessories.png",
                        categoria = "Accesorios"
                    )
                ))
                println(">>> POKESHOP HUB: 26 NUEVOS PRODUCTOS REALES INYECTADOS CON ÉXITO <<<")
            }
        }
    }
}
