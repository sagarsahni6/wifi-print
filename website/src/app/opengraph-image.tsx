import { ImageResponse } from "next/og";

export const alt = "Printora – Print from any phone. Keep your printer.";
export const size = {
  width: 1200,
  height: 630,
};
export const contentType = "image/png";

export default async function Image() {
  return new ImageResponse(
    (
      <div
        style={{
          height: "100%",
          width: "100%",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          backgroundColor: "#030712",
          fontFamily: "sans-serif",
          padding: "60px",
          color: "white",
        }}
      >
        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            marginBottom: "32px",
          }}
        >
          <span
            style={{
              fontSize: "40px",
              fontWeight: 800,
              letterSpacing: "-0.03em",
              color: "#38BDF8",
            }}
          >
            PRINTORA
          </span>
        </div>

        <div
          style={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            justifyContent: "center",
            marginBottom: "20px",
          }}
        >
          <span
            style={{
              fontSize: "46px",
              fontWeight: 900,
              letterSpacing: "-0.03em",
              color: "#F8FAFC",
              textAlign: "center",
            }}
          >
            Turn Any Windows Printer
          </span>
          <span
            style={{
              fontSize: "46px",
              fontWeight: 900,
              letterSpacing: "-0.03em",
              color: "#38BDF8",
              textAlign: "center",
            }}
          >
            Into a Printer You Can Use From Anywhere.
          </span>
        </div>

        <div
          style={{
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <span
            style={{
              fontSize: "24px",
              color: "#94A3B8",
            }}
          >
            Android Native • iPhone QR Web Print • Windows Print Host
          </span>
        </div>
      </div>
    ),
    {
      ...size,
    }
  );
}
