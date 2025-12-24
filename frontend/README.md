# Teleconsulta - Frontend

Frontend do sistema de teleconsulta desenvolvido com Next.js 14, TypeScript e Tailwind CSS.

## 🚀 Tecnologias

- **Next.js 14** - Framework React com App Router
- **TypeScript** - Tipagem estática
- **Tailwind CSS** - Estilização
- **React Hook Form** - Gerenciamento de formulários
- **Zod** - Validação de schemas
- **Axios** - Cliente HTTP
- **React Hot Toast** - Notificações
- **Lucide React** - Ícones

## 📋 Pré-requisitos

- Node.js 18+ 
- npm ou yarn

## 🔧 Instalação

```bash
# Instalar dependências
npm install

# Executar em desenvolvimento
npm run dev
```

A aplicação estará disponível em: http://localhost:3000

## 🔗 Integração com Backend

O frontend está configurado para se conectar ao backend em `http://localhost:8080` por padrão.

Para alterar, crie um arquivo `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 📁 Estrutura do Projeto

```
frontend/
├── app/                    # App Router do Next.js
│   ├── login/             # Página de login
│   ├── register/          # Página de registro
│   ├── dashboard/         # Dashboard principal
│   ├── doctors/           # Listagem de médicos
│   ├── appointments/      # Gerenciamento de consultas
│   └── plans/             # Planos disponíveis
├── components/            # Componentes reutilizáveis
│   └── Layout.tsx         # Layout principal com navbar
├── contexts/              # Contextos React
│   └── AuthContext.tsx    # Contexto de autenticação
├── lib/                   # Utilitários
│   ├── api.ts            # Cliente API e funções
│   └── auth.ts           # Funções de autenticação
└── public/               # Arquivos estáticos
```

## 🔐 Autenticação

O sistema utiliza JWT para autenticação. O token é armazenado no `localStorage` e enviado automaticamente em todas as requisições.

## 🎨 Funcionalidades

### ✅ Implementado

- [x] Login e Registro
- [x] Dashboard com estatísticas
- [x] Layout responsivo com navbar
- [x] Integração com API do backend
- [x] Gerenciamento de estado de autenticação

### 🚧 Em Desenvolvimento

- [ ] Listagem de médicos
- [ ] Agendamento de consultas
- [ ] Visualização de planos
- [ ] Perfil do usuário
- [ ] Histórico de consultas

## 📝 Usuários de Teste

Use as credenciais do backend:

- **Paciente**: carlos@email.com / patient123
- **Médico**: joao.silva@teleconsulta.com / doctor123
- **Admin**: admin@teleconsulta.com / admin123

## 🛠️ Scripts Disponíveis

```bash
npm run dev      # Desenvolvimento
npm run build    # Build de produção
npm run start    # Executar build de produção
npm run lint     # Verificar código
```

## 📄 Licença

Este projeto está sob a licença MIT.

