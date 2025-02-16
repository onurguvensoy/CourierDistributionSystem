import React from 'react';
import { useParams } from 'react-router-dom';

const PackageTracking = () => {
    const { packageId } = useParams();

    return (
        <div>
            <h1 className="h3 mb-4 text-gray-800">Package Tracking</h1>
            <div className="card shadow mb-4">
                <div className="card-header py-3">
                    <h6 className="m-0 font-weight-bold text-primary">Tracking Details for Package #{packageId}</h6>
                </div>
                <div className="card-body">
                    <p>Package tracking functionality coming soon...</p>
                </div>
            </div>
        </div>
    );
};

export default PackageTracking; 