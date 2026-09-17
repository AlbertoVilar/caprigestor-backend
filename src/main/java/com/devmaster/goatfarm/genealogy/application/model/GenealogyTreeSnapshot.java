package com.devmaster.goatfarm.genealogy.application.model;

public record GenealogyTreeSnapshot(
        GenealogyTreeNode animalPrincipal,
        GenealogyTreeNode pai,
        GenealogyTreeNode mae,
        GenealogyTreeNode avoPaterno,
        GenealogyTreeNode avoPaterna,
        GenealogyTreeNode avoMaterno,
        GenealogyTreeNode avoMaterna,
        GenealogyTreeNode bisavoPaternoPai,
        GenealogyTreeNode bisavoPaternaPai,
        GenealogyTreeNode bisavoPaternoMae,
        GenealogyTreeNode bisavoPaternaMae,
        GenealogyTreeNode bisavoMaternoPai,
        GenealogyTreeNode bisavoMaternaPai,
        GenealogyTreeNode bisavoMaternoMae,
        GenealogyTreeNode bisavoMaternaMae,
        GenealogyIntegrationSnapshot integration
) {}
