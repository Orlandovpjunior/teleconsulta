import axios from 'axios'

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: `${API_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Interceptor para adicionar token JWT
api.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Interceptor para tratar erros
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// Tipos
export interface User {
  id: number
  name: string
  email: string
  phoneNumber?: string
  cpf?: string
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT'
  crm?: string
  specialty?: string
  planId?: number
  planName?: string
  active: boolean
}

export interface Plan {
  id: number
  name: string
  description?: string
  price: number
  durationMonths?: number
  maxAppointmentsMonth?: number
  hasVideoCall: boolean
  hasChat: boolean
  hasPrescription: boolean
  hasMedicalCertificate: boolean
  features: string[]
  active: boolean
}

export interface Appointment {
  id: number
  patientId: number
  patientName: string
  doctorId: number
  doctorName: string
  doctorSpecialty?: string
  scheduledAt: string
  startedAt?: string
  endedAt?: string
  status: 'SCHEDULED' | 'CONFIRMED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'
  notes?: string
  patientComplaint?: string
  diagnosis?: string
  prescription?: string
  videoRoomId?: string
  durationMinutes?: number
  createdAt: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
  cpf: string
  phoneNumber?: string
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT'
  crm?: string
  specialty?: string
}

export interface AuthResponse {
  token: string
  type: string
  userId: number
  name: string
  email: string
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT'
}

// API Functions
export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/login', data)
    return response.data
  },
  
  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post('/auth/register', data)
    return response.data
  },
}

export const userApi = {
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get('/users/me')
    return response.data
  },
  
  getAll: async (): Promise<User[]> => {
    const response = await api.get('/users')
    return response.data
  },
  
  getById: async (id: number): Promise<User> => {
    const response = await api.get(`/users/${id}`)
    return response.data
  },
  
  updateUser: async (id: number, data: Partial<User>): Promise<User> => {
    const response = await api.put(`/users/${id}`, data)
    return response.data
  },
  
  activate: async (id: number): Promise<void> => {
    await api.patch(`/users/${id}/activate`)
  },
  
  deactivate: async (id: number): Promise<void> => {
    await api.patch(`/users/${id}/deactivate`)
  },
}

export const doctorApi = {
  getAll: async (): Promise<User[]> => {
    const response = await api.get('/doctors/public')
    return response.data
  },
  
  getBySpecialty: async (specialty: string): Promise<User[]> => {
    const response = await api.get(`/doctors/public/specialty/${specialty}`)
    return response.data
  },
}

export const planApi = {
  getActivePlans: async (): Promise<Plan[]> => {
    const response = await api.get('/plans/public')
    return response.data
  },
  
  getAll: async (): Promise<Plan[]> => {
    const response = await api.get('/plans')
    return response.data
  },
  
  getById: async (id: number): Promise<Plan> => {
    const response = await api.get(`/plans/public/${id}`)
    return response.data
  },
  
  create: async (data: CreatePlanRequest): Promise<Plan> => {
    const response = await api.post('/plans', data)
    return response.data
  },
  
  update: async (id: number, data: CreatePlanRequest): Promise<Plan> => {
    const response = await api.put(`/plans/${id}`, data)
    return response.data
  },
  
  activate: async (id: number): Promise<void> => {
    await api.patch(`/plans/${id}/activate`)
  },
  
  deactivate: async (id: number): Promise<void> => {
    await api.patch(`/plans/${id}/deactivate`)
  },
  
  subscribe: async (planId: number): Promise<void> => {
    await api.post(`/plans/${planId}/subscribe`)
  },
  
  cancelSubscription: async (): Promise<void> => {
    await api.delete('/plans/subscription')
  },
}

export interface CreatePlanRequest {
  name: string
  description?: string
  price: number
  durationMonths?: number
  maxAppointmentsMonth?: number
  hasVideoCall?: boolean
  hasChat?: boolean
  hasPrescription?: boolean
  hasMedicalCertificate?: boolean
  features?: string[]
}

export const appointmentApi = {
  getAll: async (): Promise<Appointment[]> => {
    const response = await api.get('/appointments')
    return response.data
  },
  
  getById: async (id: number): Promise<Appointment> => {
    const response = await api.get(`/appointments/${id}`)
    return response.data
  },
  
  create: async (data: {
    doctorId: number
    scheduledAt: string
    patientComplaint?: string
  }): Promise<Appointment> => {
    const response = await api.post('/appointments', data)
    return response.data
  },
  
  update: async (id: number, data: Partial<Appointment>): Promise<Appointment> => {
    const response = await api.put(`/appointments/${id}`, data)
    return response.data
  },
  
  cancel: async (id: number): Promise<Appointment> => {
    const response = await api.patch(`/appointments/${id}/cancel`)
    return response.data
  },
  
  confirm: async (id: number): Promise<Appointment> => {
    const response = await api.patch(`/appointments/${id}/confirm`)
    return response.data
  },
  
  getByStatus: async (status: Appointment['status']): Promise<Appointment[]> => {
    const response = await api.get(`/appointments/status/${status}`)
    return response.data
  },
}

