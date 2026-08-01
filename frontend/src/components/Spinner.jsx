export function Spinner({ size = 20 }) {
  return <span className="spinner spin" style={{ width: size, height: size }} />;
}

export function PageLoader() {
  return (
    <div className="page-loader">
      <Spinner size={40} />
    </div>
  );
}
