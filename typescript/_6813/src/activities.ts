
export interface Endpoint {
  get(key: string): Promise<string>;
}

export const createActivities = (endpoint: Endpoint) => ({
  async myActivity(msg: string): Promise<string> {
    const name = await endpoint.get('name');
    return `${msg}: ${name}`;
  },
});

