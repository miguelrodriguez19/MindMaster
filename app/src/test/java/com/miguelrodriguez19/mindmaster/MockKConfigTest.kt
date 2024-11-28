package com.miguelrodriguez19.mindmaster

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
// Clase de configuración de pruebas con MockK
open class MockKConfigTest {

    // Ejemplo de un mock
    private val myMock = mockk<MyClass>()

    @Before
    fun setup() {
        // Configuración antes de cada prueba, si es necesario
        clearMocks(myMock)
    }

    @Test
    fun `ejemplo de configuración básica y verificación`() {
        every { myMock.someFunction() } returns "mocked result"

        val result = myMock.someFunction()

        assertEquals("mocked result", result)
        verify { myMock.someFunction() }
    }

    @Test
    fun `ejemplo de captura de argumentos`() {
        val slot = slot<String>()
        every { myMock.someFunction(capture(slot)) } returns "mocked result"

        myMock.someFunction("argumento capturado")

        assertEquals("argumento capturado", slot.captured)
    }

    @Test
    fun `ejemplo de mocks de clases finales`() {
        val finalClassMock = mockk<FinalClass>()
        every { finalClassMock.finalFunction() } returns "resultado final"

        val result = finalClassMock.finalFunction()

        assertEquals("resultado final", result)
        verify { finalClassMock.finalFunction() }
    }

    @Test
    fun `ejemplo de función suspendida`() = runBlocking {
        coEvery { myMock.suspendFunction() } returns "resultado suspendido"

        val result = myMock.suspendFunction()

        assertEquals("resultado suspendido", result)
        coVerify { myMock.suspendFunction() }
    }

    @Test
    fun `ejemplo de función estática`() {
        mockkStatic("com.example.MyClassKt")
        every { someTopLevelFunction() } returns "resultado estático"

        val result = someTopLevelFunction()

        assertEquals("resultado estático", result)
        verify { someTopLevelFunction() }
    }

    // Ejemplo de limpieza de mocks para cada prueba, si es necesario
    @After
    fun tearDown() {
        unmockkAll()
    }
}

// Clase de ejemplo para usar en los tests
class MyClass {
    fun someFunction(): String = "real result"
    fun someFunction(param: String): String = "real result with $param"
    suspend fun suspendFunction(): String = "real suspend result"
}

// Clase final de ejemplo
final class FinalClass {
    fun finalFunction(): String = "final result"
}

// Función de nivel superior de ejemplo
fun someTopLevelFunction(): String = "real top level result"
