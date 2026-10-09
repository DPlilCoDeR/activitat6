package com.m486

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

//Volem simular una cursa d'atletisme amb N corredors i un àrbitre:
//
//Els corredors (corrutines): Arriben a la caixa de sortida en un temps d'espera aleatori.
//Quan arriben, han de notificar a l'àrbitre que estan a punt i
// quedar-se suspesos en línia de meta.
//
//L'àrbitre (corrutina): Espera a rebre la confirmació de tots els corredors.
// Quan tots han confirmat la seva arribada, l'àrbitre donarà el tret de sortida.
//
//Inici de la cursa: Cap corredor pot començar a córrer abans que l'àrbitre doni el senyal.
// Quan el senyal es produeix, tots els corredors continuen la seva execució.
//
//Fes servir objectes Channel per sincronitzar l'arbitre i els corredors.

sealed interface MensajeCursa {
    data class CorredorLlest(val id: Int) : MensajeCursa
    object TretSortida : MensajeCursa
}

suspend fun main() {
    val numCorredors = 4
    val cursa = Channel<MensajeCursa>(numCorredors)

    coroutineScope {
        repeat(numCorredors) {
            val corredorID = it + 1
            launch(Dispatchers.Default) {
                delay(Random.nextLong(100, 800).milliseconds)
                println("corredor $corredorID llest")
                cursa.send(MensajeCursa.CorredorLlest(corredorID))

                for (mensaje in cursa) {
                    if (mensaje is MensajeCursa.TretSortida){
                        println("Corredor $corredorID corrent!!")
                        cursa.send(mensaje)
                        break
                    } else {
                        cursa.send(mensaje)
                        delay(2.seconds)}
                }
            }
        }

        launch(Dispatchers.Default) {
            var llestos = 0
            for (mensaje in cursa) {
                if (mensaje is MensajeCursa.CorredorLlest) {
                    llestos++
                    if (llestos == numCorredors) {
                        cursa.send(MensajeCursa.TretSortida)
                        break
                    }
                } else{
                    cursa.send(mensaje)
                }
            }

        }
    }
}