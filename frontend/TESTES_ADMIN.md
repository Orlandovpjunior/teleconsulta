# Testes do Painel Administrativo

Este documento lista todos os testes que devem ser realizados para verificar o funcionamento do painel administrativo.

## 🔐 Credenciais de Teste

**Administrador:**
- Email: `admin@teleconsulta.com`
- Senha: `admin123`

---

## ✅ Checklist de Testes

### 1. Acesso ao Painel Administrativo

- [ ] Fazer login como administrador
- [ ] Verificar se aparece o link "Administração" no menu
- [ ] Acessar `/admin` e verificar se carrega o dashboard
- [ ] Verificar se usuários não-admin são redirecionados ao tentar acessar

**Resultado esperado:** Dashboard com estatísticas (usuários, planos, consultas)

---

### 2. Gerenciamento de Usuários (`/admin/users`)

#### 2.1 Visualização
- [ ] Listar todos os usuários do sistema
- [ ] Ver informações: nome, email, tipo, status
- [ ] Verificar se médicos mostram CRM e especialidade

#### 2.2 Busca e Filtros
- [ ] Buscar por nome (ex: "Carlos")
- [ ] Buscar por email (ex: "carlos@email.com")
- [ ] Filtrar por tipo: Administrador, Médico, Paciente
- [ ] Filtrar por status: Ativos, Inativos

#### 2.3 Ativar/Desativar Usuários
- [ ] Desativar um usuário ativo
- [ ] Verificar se o status muda para "Inativo"
- [ ] Ativar um usuário inativo
- [ ] Verificar se o status muda para "Ativo"
- [ ] Tentar fazer login com usuário desativado (deve falhar)

**Resultado esperado:** 
- Usuários podem ser ativados/desativados
- Usuários desativados não conseguem fazer login
- Mensagens de sucesso aparecem

---

### 3. Gerenciamento de Planos (`/admin/plans`)

#### 3.1 Visualização
- [ ] Ver todos os planos (ativos e inativos)
- [ ] Ver informações: nome, preço, recursos, status

#### 3.2 Criar Novo Plano
- [ ] Clicar em "Novo Plano"
- [ ] Preencher formulário:
  - Nome: "Plano Teste"
  - Preço: 79.90
  - Descrição: "Plano para testes"
  - Duração: 1 mês
  - Consultas por mês: 3
  - Marcar recursos: Videochamada, Chat, Prescrição
- [ ] Salvar e verificar se aparece na lista

#### 3.3 Editar Plano
- [ ] Clicar em "Editar" em um plano existente
- [ ] Alterar o preço
- [ ] Salvar e verificar se a alteração foi aplicada

#### 3.4 Ativar/Desativar Planos
- [ ] Desativar um plano ativo
- [ ] Verificar se o plano não aparece mais em `/plans` (página pública)
- [ ] Ativar um plano inativo
- [ ] Verificar se o plano volta a aparecer em `/plans`

**Resultado esperado:**
- Planos podem ser criados, editados, ativados e desativados
- Planos inativos não aparecem para pacientes
- Validações funcionam (preço > 0, nome obrigatório)

---

### 4. Visualização de Consultas (`/admin/appointments`)

#### 4.1 Visualização
- [ ] Ver todas as consultas do sistema
- [ ] Ver informações: data/hora, paciente, médico, status, queixa

#### 4.2 Busca e Filtros
- [ ] Buscar por nome do paciente
- [ ] Buscar por nome do médico
- [ ] Buscar por especialidade
- [ ] Filtrar por status (Agendada, Confirmada, Concluída, etc.)

**Resultado esperado:**
- Todas as consultas são exibidas
- Filtros funcionam corretamente
- Informações estão completas

---

### 5. Dashboard Administrativo (`/admin`)

- [ ] Ver estatísticas:
  - Total de usuários
  - Usuários ativos
  - Total de planos
  - Planos ativos
  - Total de consultas
  - Consultas de hoje
- [ ] Clicar em "Gerenciar Usuários" e verificar redirecionamento
- [ ] Clicar em "Gerenciar Planos" e verificar redirecionamento
- [ ] Clicar em "Todas as Consultas" e verificar redirecionamento

**Resultado esperado:**
- Estatísticas estão corretas
- Links funcionam
- Números são atualizados em tempo real

---

### 6. Testes de Segurança

- [ ] Tentar acessar `/admin` sem estar logado (deve redirecionar para login)
- [ ] Tentar acessar `/admin` como paciente (deve negar acesso)
- [ ] Tentar acessar `/admin` como médico (deve negar acesso)
- [ ] Tentar acessar `/admin/users` como não-admin (deve negar acesso)
- [ ] Tentar fazer requisições diretas à API sem token (deve falhar)

**Resultado esperado:**
- Apenas administradores podem acessar páginas admin
- Mensagens de erro apropriadas são exibidas

---

### 7. Testes de Integração

- [ ] Desativar um médico e verificar se ele não aparece mais em `/doctors`
- [ ] Desativar um plano e verificar se pacientes não podem mais assinar
- [ ] Ativar um plano e verificar se pacientes podem assinar novamente
- [ ] Criar um novo plano e verificar se aparece em `/plans`

**Resultado esperado:**
- Mudanças no admin refletem em outras páginas
- Sistema está integrado corretamente

---

## 🐛 Problemas Conhecidos

Nenhum problema conhecido no momento.

---

## 📝 Notas

- Todas as ações administrativas mostram mensagens de sucesso/erro
- Mudanças são aplicadas imediatamente
- Interface é responsiva e funciona em mobile

---

## ✅ Status dos Testes

Após realizar os testes, marque os itens acima e documente qualquer problema encontrado.

