package com.devmaster.goatfarm.application.core.business.common;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class EntityFinder {

    /**
     * Tenta encontrar uma entidade usando o Supplier fornecido.
     * Se não encontrar (Optional vazio), lança ResourceNotFoundException com a mensagem especificada.
     *
     * @param finder Supplier que retorna um Optional da entidade (ex: repositorio::findById)
     * @param errorMessage Mensagem de erro para a exceção caso não encontrado
     * @param <T> Tipo da entidade
     * @return A entidade encontrada
     */
    public <T> T findOrThrow(Supplier<Optional<T>> finder, String errorMessage) {
        return finder.get()
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage));
    }
}
