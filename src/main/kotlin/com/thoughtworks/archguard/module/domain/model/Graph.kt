package com.thoughtworks.archguard.module.domain.model

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

class GraphStore<T> {
    private val nodes = CopyOnWriteArrayList<NodeWrapper<T>>()
    private val edges = CopyOnWriteArrayList<Edge<T>>()

    private var index: AtomicInteger = AtomicInteger(0)

    private fun addNode(node: T): NodeWrapper<T> {
        val i = index.incrementAndGet()
        val nodeWrapper = NodeWrapper(i, Node(node))
        nodes.add(nodeWrapper)
        return nodeWrapper
    }

    private fun addEdge(a: NodeWrapper<T>, b: NodeWrapper<T>, num: Int) {
        edges.add(Edge(a, b, num))
    }

    fun addEdge(a: T, b: T, num: Int) {
        val nodeA = addNode(a)
        val nodeB = addNode(b)
        addEdge(nodeA, nodeB, num)
    }

    fun getGraph(): Graph<T> {
        return Graph(nodes, edges)
    }
}

data class Node<T>(val node: T)
data class Graph<T>(val nodes: List<NodeWrapper<T>>, val edges: List<Edge<T>>)
data class Edge<T>(val a: NodeWrapper<T>, val b: NodeWrapper<T>, val num: Int)
data class NodeWrapper<T>(val id: Int, val node: Node<T>)