# capacitor-native-audio-notification

Used to push notification with using FCM even if app is killed

## Install

```bash
npm install capacitor-native-audio-notification
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`configure()`](#configure)
* [`testNotification(...)`](#testnotification)
* [`setMerchantInfo(...)`](#setmerchantinfo)
* [`toggleNotifications(...)`](#togglenotifications)
* [`playTts(...)`](#playtts)
* [`stopTts()`](#stoptts)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### configure()

```typescript
configure() => Promise<void>
```

--------------------


### testNotification(...)

```typescript
testNotification(options: { amount: string; currency?: string; customerName?: string; transactionId?: string; timestamp?: number; }) => Promise<void>
```

| Param         | Type                                                                                                                   |
| ------------- | ---------------------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ amount: string; currency?: string; customerName?: string; transactionId?: string; timestamp?: number; }</code> |

--------------------


### setMerchantInfo(...)

```typescript
setMerchantInfo(options: { businessName: string; }) => Promise<{ businessName: string; }>
```

| Param         | Type                                   |
| ------------- | -------------------------------------- |
| **`options`** | <code>{ businessName: string; }</code> |

**Returns:** <code>Promise&lt;{ businessName: string; }&gt;</code>

--------------------


### toggleNotifications(...)

```typescript
toggleNotifications(options: { enabled?: boolean; }) => Promise<{ enabled: boolean; }>
```

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`options`** | <code>{ enabled?: boolean; }</code> |

**Returns:** <code>Promise&lt;{ enabled: boolean; }&gt;</code>

--------------------


### playTts(...)

```typescript
playTts(options: { text?: string; }) => Promise<void>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ text?: string; }</code> |

--------------------


### stopTts()

```typescript
stopTts() => Promise<void>
```

--------------------

</docgen-api>
