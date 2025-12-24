import { cookies } from 'next/headers'

export async function getServerSession() {
  const cookieStore = await cookies()
  const token = cookieStore.get('token')?.value
  
  if (!token) {
    return null
  }
  
  try {
    const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
    const response = await fetch(`${API_URL}/api/users/me`, {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
      cache: 'no-store',
    })
    
    if (response.ok) {
      const user = await response.json()
      return { user, token }
    }
  } catch (error) {
    console.error('Error validating session:', error)
  }
  
  return null
}

