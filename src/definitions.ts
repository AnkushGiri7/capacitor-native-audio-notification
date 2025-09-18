export interface NativeAudioNotificationPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  configure(): Promise<void>;
  testNotification(options: {
    amount: string;
    currency?: string;
    customerName?: string;
    transactionId?: string;
    timestamp?: number;
  }): Promise<void>;
  setMerchantInfo(options: { businessName: string }): Promise<{ businessName: string }>;
  toggleNotifications(options: { enabled?: boolean }): Promise<{ enabled: boolean }>;
  playTts(options: { text?: string }): Promise<void>;
  stopTts(): Promise<void>;
}