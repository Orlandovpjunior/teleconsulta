'use client'

import React, { createContext, useContext, useState, useEffect } from 'react'
import { User, authApi } from '@/lib/api'
import { useRouter } from 'next/navigation'
import toast from 'react-hot-toast'

interface AuthContextType {
  user: User | null
  loading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (data: RegisterData) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

interface RegisterData {
  name: string
  email: string
  password: string
  cpf: string
  phoneNumber?: string
  role: 'ADMIN' | 'DOCTOR' | 'PATIENT'
  crm?: string
  specialty?: string
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const router = useRouter()

  useEffect(() => {
    loadUser()
  }, [])

  const loadUser = async () => {
    try {
      const token = localStorage.getItem('token')
      if (token) {
        const userData = localStorage.getItem('user')
        if (userData) {
          setUser(JSON.parse(userData))
        } else {
          // Tentar buscar do servidor
          const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/users/me`, {
            headers: {
              'Authorization': `Bearer ${token}`,
            },
          })
          if (response.ok) {
            const userData = await response.json()
            setUser(userData)
            localStorage.setItem('user', JSON.stringify(userData))
          } else {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
          }
        }
      }
    } catch (error) {
      console.error('Error loading user:', error)
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string) => {
    try {
      const response = await authApi.login({ email, password })
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
      }))
      setUser({
        id: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
        active: true,
      })
      toast.success('Login realizado com sucesso!')
      router.push('/dashboard')
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao fazer login'
      toast.error(message)
      throw error
    }
  }

  const register = async (data: RegisterData) => {
    try {
      const response = await authApi.register(data)
      localStorage.setItem('token', response.token)
      localStorage.setItem('user', JSON.stringify({
        id: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
      }))
      setUser({
        id: response.userId,
        name: response.name,
        email: response.email,
        role: response.role,
        active: true,
      })
      toast.success('Cadastro realizado com sucesso!')
      router.push('/dashboard')
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao fazer cadastro'
      toast.error(message)
      throw error
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
    router.push('/login')
    toast.success('Logout realizado com sucesso!')
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

