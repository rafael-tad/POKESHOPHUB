package com.pokeshophub.controller

import com.pokeshophub.dto.*
import com.pokeshophub.model.GastoIngreso
import com.pokeshophub.model.TipoMovimiento
import com.pokeshophub.repository.ClienteRepository
import com.pokeshophub.repository.GastoIngresoRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/wallet")
class WalletController(
    private val clienteRepository: ClienteRepository,
    private val gastoIngresoRepository: GastoIngresoRepository
) {

    @GetMapping("/historial/{clienteId}")
    fun obtenerHistorial(@PathVariable clienteId: Long): List<GastoIngreso> {
        return gastoIngresoRepository.findByClienteIdOrderByFechaDesc(clienteId)
    }

    // ADMIN: Añadir o retirar saldo a un usuario (Store Credit)
    @PostMapping("/admin/ajustar-saldo/{clienteId}")
    fun ajustarSaldo(
        @PathVariable clienteId: Long,
        @RequestBody request: AjustarSaldoRequest
    ): ResponseEntity<MensajeResponse> {
        val cliente = clienteRepository.findById(clienteId).orElse(null)
            ?: return ResponseEntity.notFound().build()

        cliente.saldo += request.importe
        clienteRepository.save(cliente)

        val tipo = if (request.importe >= 0) TipoMovimiento.INGRESO else TipoMovimiento.GASTO
        gastoIngresoRepository.save(
            GastoIngreso(
                clienteId = clienteId,
                tipo = tipo,
                descripcion = request.descripcion,
                importe = Math.abs(request.importe),
                categoria = "AJUSTE",
                fecha = LocalDate.now()
            )
        )

        return ResponseEntity.ok(MensajeResponse("Saldo actualizado correctamente. Nuevo saldo: ${cliente.saldo}€", true))
    }
}
