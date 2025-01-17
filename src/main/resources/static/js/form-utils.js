function updateAccountOwner() {
    console.log('Função updateAccountOwner chamada.');

    // Obter o elemento do select
    const userSelect = document.getElementById('userId');
    if (!userSelect) {
        console.error('Elemento select com id "userId" não encontrado.');
        return;
    }

    // Obter a opção selecionada
    const selectedOption = userSelect.options[userSelect.selectedIndex];
    if (!selectedOption) {
        console.error('Nenhuma opção selecionada encontrada.');
        return;
    }

    // Obter o valor do atributo data-name
    const userName = selectedOption.getAttribute('data-name');
    console.log('Nome do usuário selecionado:', userName);

    // Atualizar o campo "Nome do Titular"
    const ownerNameField = document.getElementById('ownerName');
    if (ownerNameField) {
        ownerNameField.value = userName || '';
        console.log('Campo "Nome do Titular" atualizado com:', userName);
    } else {
        console.error('Campo de texto com id "ownerName" não encontrado.');
    }
}