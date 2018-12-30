package com.majewski.hivemindbt.data

internal class SharedData {

    var nbOfClients: Byte = 0
    internal set

    var clientId: Byte = 0
    internal set

    private val clients = HashMap<Byte, HashMap<Byte, ByteArray>>()

    private val nameIdDictionary = HashMap<String, Byte>()

    fun addElement(name: String, id: Byte) {
        clients[id] = HashMap()
        nameIdDictionary[name] = id
    }

    fun setElementValue(elementId: Byte, value: ByteArray) {
        val elementData = clients[elementId] ?: throw NoSuchElementException("Element with given id/name not found.")
        elementData[clientId] = value
}

    fun getElementValue(name: String, clientId: Byte): ByteArray? {
        val elementId = nameIdDictionary[name]
        return getElementValue(elementId, clientId)
    }

    fun getElementValue(elementId: Byte?, clientId: Byte): ByteArray? {
        val elementData = clients[elementId]
        return elementData?.get(clientId)
    }

    fun getElementId(name: String) = nameIdDictionary[name]

    fun getElementName(id: Byte) = nameIdDictionary.filter { it.value == id }.keys.first()

    fun setElementValueFromClient(elementId: Byte, clientId: Byte, value: ByteArray) {
        clients[elementId]?.put(clientId, value)
    }
}

