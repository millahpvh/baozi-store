package com.camila.baozistore.controller;

import com.camila.baozistore.model.Cliente;
import com.camila.baozistore.model.Pedido;
import com.camila.baozistore.model.Produto;
import com.camila.baozistore.repository.ClienteRepository;
import com.camila.baozistore.repository.PedidoRepository;
import com.camila.baozistore.repository.ProdutoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoController(
            PedidoRepository pedidoRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository) {

        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
    }

    public record DadosPedido(
            @NotNull Long clienteId,
            @NotNull Long produtoId,
            @NotNull @Min(1) Integer quantidade) {
    }

    @PostMapping
    public ResponseEntity<Pedido> criar(
            @Valid @RequestBody DadosPedido dados) {

        Cliente cliente = clienteRepository.findById(dados.clienteId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado"));

        Produto produto = produtoRepository.findById(dados.produtoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (!Boolean.TRUE.equals(produto.getEstoque())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Produto indisponível");
        }

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(dados.quantidade());

        Pedido salvo = pedidoRepository.save(pedido);

        return ResponseEntity
                .created(URI.create("/pedidos/" + salvo.getId()))
                .body(salvo);
    }

    @GetMapping
    public List<Pedido> listar() {
        return pedidoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(
            @PathVariable("id") Long id) {

        return pedidoRepository.findById(id)
                .map(pedido -> ResponseEntity.ok(pedido))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable("id") Long id) {

        if (!pedidoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}