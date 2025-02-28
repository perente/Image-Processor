import React, { useState, useRef, useEffect } from "react";
import axios from "axios";
import { Circles } from 'react-loader-spinner';
import { ToastContainer, toast } from 'react-toastify';
import { AiFillFileZip } from "react-icons/ai";
import Confetti from "react-confetti";
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';

const ImageProcessor: React.FC = () => {
  const [file, setFile] = useState<File | null>(null);
  const [previewURL, setPreviewURL] = useState<string | null>(null);
  const [processedImages, setProcessedImages] = useState<{ action: string; url: string }[]>([]);
  const [zipBlob, setZipBlob] = useState<Blob | null>(null);
  const [grayscale, setGrayscale] = useState(false);
  const [noise, setNoise] = useState(false);
  const [brightness, setBrightness] = useState(false);
  const [brightnessValue, setBrightnessValue] = useState(50);
  const [saltProb, setSaltProb] = useState(0.01);
  const [pepperProb, setPepperProb] = useState(0.01);
  const [loading, setLoading] = useState(false);
  const [showConfetti, setShowConfetti] = useState(false);
  const [windowSize, setWindowSize] = useState({ width: window.innerWidth, height: window.innerHeight });

  useEffect(() => {
    const handleResize = () => {
      setWindowSize({ width: window.innerWidth, height: window.innerHeight });
    };

    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  const processedImagesRef = useRef<HTMLDivElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
      setPreviewURL(URL.createObjectURL(e.target.files[0]));
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!file) {
      toast.error("Please upload an image.");
      return;
    }

    if (!grayscale && !noise && !brightness) {
      toast.error("Please select a processing option");
      return;
    }    

    const formData = new FormData();
    formData.append("file", file);

    const params: string[] = [];
    if (grayscale) params.push("action=grayscale");
    if (noise) params.push(`action=noise&salt_prob=${saltProb}&pepper_prob=${pepperProb}`);
    if (brightness) params.push(`action=brightness&brightness=${brightnessValue}`);

    try {
      setLoading(true);
      toast.info("Processing image...");

      const response = await axios.post(
        `http://localhost:5000/process-image?${params.join("&")}`,
        formData
      );

      const { processed_images, zip_file } = response.data;

      const imagesData = await Promise.all(
        processed_images.map(async (imgData: { action: string; image: string }) => {
          const imageBlob = await (await fetch(`data:image/png;base64,${imgData.image}`)).blob();
          return { action: imgData.action, url: URL.createObjectURL(imageBlob) };
        })
      );
      setProcessedImages(imagesData);

      const zipBlobData = await (await fetch(`data:application/zip;base64,${zip_file}`)).blob();
      setZipBlob(zipBlobData);

      toast.success(
        <span className="d-flex align-items-center gap-2">
          All images processed successfully!
        </span>
      );
      
      setShowConfetti(true);
      setTimeout(() => setShowConfetti(false), 5000); 
      

      setTimeout(() => {
        processedImagesRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
      }, 500);
    } catch (error) {
      console.error("Error processing image:", error);
      toast.error("Failed to process image");
    } finally {
      setLoading(false);
    }
  };

  const handleZipDownload = () => {
    if (zipBlob) {
      const link = document.createElement("a");
      link.href = URL.createObjectURL(zipBlob);
      link.setAttribute("download", "processed_images.zip");
      document.body.appendChild(link);
      link.click();
    } else {
      toast.error("No ZIP file available for download");
    }
  };

  return (
    <div className="container pt-3 fs-5 bg-light">
      {showConfetti && (
        <Confetti
          width={windowSize.width}
          height={windowSize.height}
          numberOfPieces={300}
          recycle={false} 
          gravity={0.2} 
        />
      )}
      <ToastContainer position="top-center" autoClose={2000} />
      <form onSubmit={handleSubmit} className="mb-4">
        <input type="file" className="form-control mb-3 fs-5" accept="image/*" onChange={handleFileChange} />
        {previewURL && (
          <div className="text-center">
            <h5>Original Image Preview:</h5>
            <img src={previewURL} alt="Preview" width="300" className="img-thumbnail" />
          </div>
        )}

        <div className="form-check d-flex align-items-center">
          <input className="form-check-input" type="checkbox" style={{ width: "1.75rem", height: "1.75rem" }} checked={grayscale} onChange={() => setGrayscale(!grayscale)} />
          <label className="form-check-label ms-2">Grayscale</label>
        </div>

        <div className="form-check d-flex align-items-center">
          <input className="form-check-input" type="checkbox" style={{ width: "1.75rem", height: "1.75rem" }} checked={noise} onChange={() => setNoise(!noise)} />
          <label className="form-check-label ms-2">Add Noise</label>
        </div>

        {noise && (
          <div className="row mb-3 mt-3">
            <div className="col">
              <label>Salt Probability:</label>
              <input type="number" className="form-control" style={{ height: "2.5rem" }} value={saltProb} step="0.01" min="0" max="1"
                     onChange={(e) => setSaltProb(parseFloat(e.target.value))} />
            </div>
            <div className="col">
              <label>Pepper Probability:</label>
              <input type="number" className="form-control" style={{ height: "2.5rem" }} value={pepperProb} step="0.01" min="0" max="1"
                     onChange={(e) => setPepperProb(parseFloat(e.target.value))} />
            </div>
          </div>
        )}

        <div className="form-check d-flex align-items-center">
          <input className="form-check-input" type="checkbox" style={{ width: "1.75rem", height: "1.75rem" }} checked={brightness} onChange={() => setBrightness(!brightness)} />
          <label className="form-check-label ms-2">Adjust Brightness</label>
        </div>

        {brightness && (
          <input type="range" className="form-range mt-3" min="0" max="100" value={brightnessValue}
                 onChange={(e) => setBrightnessValue(parseInt(e.target.value))} />
        )}

        <button type="submit" className="btn btn-primary w-100 mt-3 fs-5" disabled={loading}>
          {loading ? "Processing..." : "Process Image"}
        </button>
      </form>

      {loading && (
        <div className="d-flex justify-content-center">
          <Circles height="80" width="80" color="#4fa94d" />
        </div>
      )}

      {processedImages.length > 0 && (
        <div className="text-center" ref={processedImagesRef}>
          <h5>Processed Image Previews:</h5>
          <div className="d-flex justify-content-center flex-wrap">
            {processedImages.map((img, idx) => (
              <div key={idx} className="m-2">
                <h6 className="">{img.action}</h6>
                <img src={img.url} alt={img.action} width="300" className="img-thumbnail" />
              </div>
            ))}
          </div>
        </div>
      )}
      {zipBlob && (
        <div className="text-center mt-3 pb-3">
          <button className="btn btn-success align-items-center justify-content-center gap-1 fs-5" onClick={handleZipDownload}>
            <AiFillFileZip style={{ fontSize: "1.5rem", verticalAlign: "middle" }} /> Download All (ZIP)
          </button>
        </div>
      )}
    </div>
  );
};

export default ImageProcessor;
