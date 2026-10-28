export class ApiError extends Error {
  status: number;
  body?: Record<string, any>;
  constructor(status: number, message: string, body?: Record<string, any>) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}
