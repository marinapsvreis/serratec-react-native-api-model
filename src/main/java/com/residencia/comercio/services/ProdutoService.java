package com.residencia.comercio.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencia.comercio.dtos.CategoriaDTO;
import com.residencia.comercio.dtos.ProdutoDTO;
import com.residencia.comercio.entities.Categoria;
import com.residencia.comercio.entities.Produto;
import com.residencia.comercio.repositories.ProdutoRepository;

@Service
public class ProdutoService {
	@Autowired
	ProdutoRepository produtoRepository;

	@Autowired
	ArquivoService arquivoService;

	public List<Produto> findAll() {
		return produtoRepository.findAll();
	}

	public List<ProdutoDTO> findAllDTO() {
		List<Produto> listProduto = produtoRepository.findAll();
		return listProduto.stream()
				.map(entity -> new ProdutoDTO(entity.getIdProduto(), entity.getSku(), entity.getNomeProduto(),
						entity.getDescricaoProduto(), entity.getImagemProduto(), entity.getPrecoProduto(),
						entity.getFornecedor().getRazaoSocial(),
						entity.getFornecedor().getIdFornecedor(),
						entity.getCategoria().getNomeCategoria(),
						entity.getCategoria().getIdCategoria()))
				.collect(Collectors.toList());
	}

	public Produto findById(Integer id) {
		return produtoRepository.findById(id).isPresent() ? produtoRepository.findById(id).get() : null;
	}

	public Produto save(Produto produto) {
		return produtoRepository.save(produto);
	}

	public Produto update(Produto produto) {
		return produtoRepository.save(produto);
	}

	public Produto updateComId(Produto produto, Integer id) {
		Produto produtoBD = produtoRepository.findById(id).isPresent() ? produtoRepository.findById(id).get() : null;

		Produto produtoAtualizado = null;
		if (null != produtoBD) {
			produtoBD.setCategoria(produto.getCategoria());
			// ...
			produtoAtualizado = produtoRepository.save(produtoBD);
		}
		return produtoAtualizado;
	}

	public void delete(Produto produto) {
		produtoRepository.delete(produto);
	}

	public void deletePorId(Integer id) {
		produtoRepository.deleteById(id);
	}

	public Produto saveProdutoComFoto(@RequestPart("produto") String produto, @RequestPart("file") MultipartFile file) {
		Produto produtoFromJson = convertProdutoFromStringJson(produto);
		Produto novoProduto = produtoRepository.save(produtoFromJson);

		// Limpeza no nome do arquivo
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		// Concatena o id do produto ao nome do arquivo
		fileName = novoProduto.getIdProduto().toString() + "_" + fileName;

		// Seta no produto recem criado o nome da imagem
		novoProduto.setImagemProduto(fileName);

		// Armazena a foto no diretorio
		arquivoService.storeFile(file, fileName);

		// Atualiza o produto recem criado, agora com o nome da imagem
		return produtoRepository.save(novoProduto);
	}

	private Produto convertProdutoFromStringJson(String produtoJson) {
		Produto produto = new Produto();

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			produto = objectMapper.readValue(produtoJson, Produto.class);
		} catch (IOException err) {
			System.out.printf("Ocorreu um erro ao tentar converter a string json para um inst??ncia da entidade Produto",
					err.toString());
		}

		return produto;
	}

}
