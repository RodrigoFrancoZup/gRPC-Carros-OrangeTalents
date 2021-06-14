package br.com.zup.carros

import br.com.zup.CarroRequest
import br.com.zup.CarrosGrpcServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Singleton


//Rodar testes por linha de comando: .\gradlew.bat test
@MicronautTest(transactional = false)
internal class CarrosEndpointTest(
    val repository: CarrosRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    @Test
    fun `deve adicionar um novo carro`() {
        // Cenário
        // repository.deleteAll()

        // Ação
        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("Golf")
                .setPlaca("ABC-9999")
                .build()
        )

        // Validação
        assertNotNull(response.id)
        assertTrue(repository.existsById(response.id))
    }

    @Test
    fun `nao deve adicionar novo carro quando carro com placa ja existente`() {
        // Cenário
        // repository.deleteAll()
        val existente = repository.save(Carro("Palio", "OIP-9876"))

        // Ação
        val request = CarroRequest.newBuilder()
            .setModelo("Ferrari")
            .setPlaca(existente.placa)
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.adicionar(request)
        }

        //Validação
        assertEquals(Status.ALREADY_EXISTS.code, error.status.code)
        assertEquals("carro com placa existente", error.status.description)

    }

    @Test
    fun `nao deve adicionar novo carro quando dados de entrada forem invalidos` (){
        // Cenário
        // repository.deleteAll()


        // Ação
        val request = CarroRequest.newBuilder()
            .setModelo("")
            .setPlaca("")
            .build()

        val error = assertThrows(StatusRuntimeException::class.java) {
            grpcClient.adicionar(request)
        }

        //Validação
        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("dados de entrada inválidos", error.status.description)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)

        }
    }
}

